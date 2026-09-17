/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.state

import android.app.Activity
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Process
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.extension.toRational
import java.lang.ref.WeakReference

private const val TAG = "PipManager"

/**
 * The system rejects aspect ratios outside `[1/2.39, 2.39]`. Those bounds stay slightly inside that range, to be safe from rounding.
 */
private val MIN_ASPECT_RATIO = Rational(1000, 2385)
private val MAX_ASPECT_RATIO = Rational(2385, 1000)

/**
 * `AppOpsManager.OPSTR_PICTURE_IN_PICTURE` is not part of the public SDK.
 */
private const val OpPictureInPicture = "android:picture_in_picture"

/**
 * Creates a [PipManager] that is remembered across compositions.
 *
 * As long as it is in composition, it keeps the [Activity]'s Picture-in-Picture parameters up to date, so that the system can animate the
 * transition, even when Picture-in-Picture is not entered through [PipManager.enter].
 *
 * @param player The [Player] to get the Picture-in-Picture aspect ratio from. Set [PipManager.ratio] to use another aspect ratio.
 * @param autoEnterEnabled Whether the [Activity] automatically enters Picture-in-Picture when the user leaves it. Requires Android S.
 * @return A [PipManager] instance.
 */
@Composable
fun rememberPipManager(
    player: Player? = null,
    autoEnterEnabled: Boolean = false,
): PipManager {
    val activity = checkNotNull(LocalActivity.current as? ComponentActivity) {
        "rememberPipManager() requires the local Activity to be a ComponentActivity"
    }
    val pipManager = remember(activity) { PipManagerImpl(activity) }

    DisposableEffect(pipManager, player) {
        pipManager.attach(player)

        onDispose {
            pipManager.detach()
        }
    }

    SideEffect {
        pipManager.autoEnterEnabled = autoEnterEnabled
    }

    return pipManager
}

/**
 * Manages the Picture-in-Picture mode of an [Activity].
 *
 * Get an instance with [rememberPipManager].
 */
interface PipManager {
    /**
     * Whether the device and the [Activity] support Picture-in-Picture. It is `false` when the [Activity] is missing
     * `android:supportsPictureInPicture="true"` in the manifest.
     */
    val isSupported: Boolean

    /**
     * Whether the user allows Picture-in-Picture for this application.
     */
    val isAllowed: Boolean

    /**
     * Whether the [Activity] is currently in Picture-in-Picture mode.
     */
    val isInPictureInPicture: Boolean

    /**
     * Whether the [Activity] is currently going to Picture-in-Picture mode.
     */
    val isTransitioning: Boolean

    /**
     * Bounds, in window coordinates, of the content that the system animates into and out of the Picture-in-Picture window, or `null` if they are
     * not known yet.
     */
    var sourceRect: Rect?

    /**
     * Aspect ratio of the Picture-in-Picture window. Setting it overrides the aspect ratio computed from the [Player] video size, and setting it
     * back to `null` restores it.
     *
     * It is coerced into the range supported by the system.
     */
    var ratio: Rational?

    /**
     * Whether the [Activity] automatically enters Picture-in-Picture mode when the user leaves it, which gives a smoother transition than entering
     * it from [onUserLeaveHint][Activity.onUserLeaveHint]. It has no effect below Android S.
     */
    var autoEnterEnabled: Boolean

    /**
     * Enter Picture-in-Picture mode. It does nothing when [isSupported] is `false`.
     */
    fun enter()
}

private class PipManagerImpl(activity: ComponentActivity) : PipManager {
    private val activityRef = WeakReference(activity)
    private val pictureInPictureModeObserver = Consumer<PictureInPictureModeChangedInfo> { changedInfo ->
        isInPictureInPicture = changedInfo.isInPictureInPictureMode
        isTransitioning = false
    }
    private val playerListener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            playerRatio = videoSize.toPictureInPictureRatio()
            updatePictureInPictureParams()
        }
    }

    /**
     * The user can change the Picture-in-Picture setting of the application while it is in the background, so [isAllowed] is refreshed each time
     * the [Activity] is resumed.
     *
     * It goes through `this.activity`, and not through the constructor parameter, to not hold a strong reference to the [Activity].
     */
    private val lifecycleObserver = LifecycleEventObserver { t, event ->
        /*if (event == Lifecycle.Event.ON_RESUME) {
            isAllowed = this.activity?.isPictureInPictureAllowed() == true
        }*/
    }

    private var player: Player? = null
    private var playerRatio: Rational? = null
    private var ratioOverride: Rational? = null

    private val activity: ComponentActivity?
        get() = activityRef.get()

    override val isSupported = activity.supportsPictureInPicture()

    override var isAllowed by mutableStateOf(activity.isPictureInPictureAllowed())
        private set

    override var isInPictureInPicture by mutableStateOf(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInPictureInPictureMode,
    )
        private set

    override var isTransitioning by mutableStateOf(false)
        private set

    override var sourceRect: Rect? = null
        set(value) {
            if (field == value) return

            field = value
            updatePictureInPictureParams()
        }

    override var ratio: Rational?
        get() = ratioOverride ?: playerRatio
        set(value) {
            if (ratioOverride == value) return

            ratioOverride = value
            updatePictureInPictureParams()
        }

    override var autoEnterEnabled: Boolean = false
        set(value) {
            if (field == value) return

            field = value
            updatePictureInPictureParams()
        }

    override fun enter() {
        if (!isSupported) return
        isAllowed = this.activity?.isPictureInPictureAllowed() == true
        if (!isAllowed) return
        activity?.runCatchingPictureInPicture {
            isTransitioning = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(pictureInPictureParams())
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // is supported should have caught that before
                    @Suppress("DEPRECATION")
                    enterPictureInPictureMode()
                }
            }
        }
    }

    fun attach(player: Player?) {
        this.player = player
        playerRatio = player?.videoSize?.toPictureInPictureRatio()
        player?.addListener(playerListener)
        activity?.addOnPictureInPictureModeChangedListener(pictureInPictureModeObserver)
        activity?.lifecycle?.addObserver(lifecycleObserver)
        updatePictureInPictureParams()
    }

    fun detach() {
        activity?.lifecycle?.removeObserver(lifecycleObserver)
        activity?.removeOnPictureInPictureModeChangedListener(pictureInPictureModeObserver)
        player?.removeListener(playerListener)
        player = null
    }

    private fun updatePictureInPictureParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !isSupported) return

        activity?.runCatchingPictureInPicture {
            setPictureInPictureParams(pictureInPictureParams())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pictureInPictureParams(): PictureInPictureParams {
        return PictureInPictureParams.Builder()
            .setAspectRatio(ratio)
            .setSourceRectHint(sourceRect)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(autoEnterEnabled)
                }
            }
            .build()
    }
}

/**
 * The Picture-in-Picture APIs throw when the [Activity] is not declared with `android:supportsPictureInPicture="true"`, which can only be detected
 * this way: [ActivityInfo][android.content.pm.ActivityInfo] does not expose that flag publicly.
 */
private inline fun ComponentActivity.runCatchingPictureInPicture(block: ComponentActivity.() -> Unit) {
    try {
        block()
    } catch (exception: IllegalStateException) {
        Log.w(TAG, "Picture-in-Picture is not available. Is android:supportsPictureInPicture=\"true\" set for this Activity?", exception)
    }
}

private fun ComponentActivity.supportsPictureInPicture(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
}

// checkOpNoThrow() was renamed unsafeCheckOpNoThrow() in Android Q, which is in turn deprecated in more recent SDKs. They all behave the same, so
// the oldest one is used to support every SDK with a single call. Any failure is reported as allowed, to not hide a working button.
private fun ComponentActivity.isPictureInPictureAllowed(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

    val appOpsManager = getSystemService(AppOpsManager::class.java)
    val mode = runCatching {
        appOpsManager?.checkOpNoThrow(OpPictureInPicture, Process.myUid(), packageName)
    }.getOrNull() ?: AppOpsManager.MODE_ALLOWED

    return mode == AppOpsManager.MODE_ALLOWED
}

private fun VideoSize.toPictureInPictureRatio(): Rational? {
    if (this == VideoSize.UNKNOWN) return null

    return toRational().coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO)
}
