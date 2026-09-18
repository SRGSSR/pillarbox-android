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
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.extension.toRational
import java.lang.ref.WeakReference

/**
 * Remembers the value of a [PipManager] created based on the passed [Player] and launches a
 * coroutine to listen to the [Player's][Player] changes. If the [Player] instance changes between
 * compositions, this produces and remembers a new [PipManager].
 *
 * As long as it is in composition, it keeps the [Activity]'s Picture-in-Picture parameters up to date, so that the system can animate the
 * transition, even when Picture-in-Picture is not entered through [PipManager.enter].
 *
 * @param player The [Player] to get the Picture-in-Picture aspect ratio from. Set [PipManager.ratio] to use another aspect ratio.
 * @param autoEnterEnabled Whether the [Activity] automatically enters Picture-in-Picture when the user leaves it. Requires Android S.
 * @return A [PipManager] instance.
 *
 * **Sample usage:**
 *
 * ```kotlin
 * val pipManager = rememberPipManager(player = player, autoEnterEnabled = true),
 * ```
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
 * Represent the Picture in Picture manager
 * Manages the Picture-in-Picture mode of an [Activity].
 *
 * Get an instance with [rememberPipManager].
 */
interface PipManager {
    /**
     * Represents whether the device and the [Activity] support Picture-in-Picture. It is `false` when the [Activity] is missing
     * `android:supportsPictureInPicture="true"` in the manifest.
     */
    val isSupported: Boolean

    /**
     * Represents whether the user allows Picture-in-Picture for this application.
     */
    val isAllowed: Boolean

    /**
     * Represents whether the [Activity] is currently in Picture-in-Picture mode.
     */
    val isInPictureInPicture: Boolean

    /**
     * Represents whether the [Activity] is currently going to Picture-in-Picture mode.
     */
    val isTransitioning: Boolean

    /**
     * Represents the bounds, in window coordinates, of the content that the system animates into and out of the Picture-in-Picture window,
     * or `null` if they are not known yet.
     */
    var sourceRect: Rect?

    /**
     * Represents the aspect ratio of the Picture-in-Picture window.
     */
    var ratio: Rational?

    /**
     * Represents whether the [Activity] automatically enters Picture-in-Picture mode when the user leaves it, which gives a smoother transition
     * than entering it from [onUserLeaveHint][Activity.onUserLeaveHint]. It has no effect below Android S.
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
            playerRatio = videoSize.toRational()
            updatePictureInPictureParams()
        }
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
        playerRatio = player?.videoSize?.toRational()
        player?.addListener(playerListener)
        activity?.addOnPictureInPictureModeChangedListener(pictureInPictureModeObserver)
        updatePictureInPictureParams()
    }

    fun detach() {
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

    /**
     * The Picture-in-Picture APIs throw when the [Activity] is not declared with `android:supportsPictureInPicture="true"`, which can only
     * be detected this way: [ActivityInfo][android.content.pm.ActivityInfo] does not expose that flag publicly.
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

    /**
     * Represents the Picture in Picture authorization/compatibility
     *
     * @return true if Picture in Picture is allowed by the Android version (>= Android 26), and is enabled for the application by the user.
     */
    private fun ComponentActivity.isPictureInPictureAllowed(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val appOpsManager = getSystemService(AppOpsManager::class.java)
        val mode = runCatching {
            appOpsManager?.checkOpNoThrow(OpPictureInPicture, Process.myUid(), packageName)
        }.getOrNull() ?: AppOpsManager.MODE_ALLOWED

        return mode == AppOpsManager.MODE_ALLOWED
    }

    companion object {

        private const val TAG = "PipManager"

        /**
         * `AppOpsManager.OPSTR_PICTURE_IN_PICTURE` is not part of the public SDK.
         */
        private const val OpPictureInPicture = "android:picture_in_picture"
    }
}
