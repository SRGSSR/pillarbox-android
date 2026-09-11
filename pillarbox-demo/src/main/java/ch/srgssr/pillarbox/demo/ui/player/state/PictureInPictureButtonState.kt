/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.state

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.roundToIntRect
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.app.PictureInPictureParamsCompat
import androidx.core.util.Consumer
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.extension.RATIONAL_ONE
import ch.srgssr.pillarbox.player.extension.toRational
import java.lang.ref.WeakReference

/**
 * The smallest aspect ratio accepted by [Activity.enterPictureInPictureMode], as documented by [PictureInPictureParams.Builder.setAspectRatio].
 */
private val MinAspectRatio = Rational(100, 239)

/**
 * The biggest aspect ratio accepted by [Activity.enterPictureInPictureMode], as documented by [PictureInPictureParams.Builder.setAspectRatio].
 */
private val MaxAspectRatio = Rational(239, 100)

/**
 * Creates a [PictureInPictureButtonState] that is remembered across compositions.
 *
 * It is recommended to use [rememberPictureInPictureButtonState] with a [Player], or with [PictureInPictureParams] for Android O and above,
 * for more control over the Picture-in-Picture behavior.
 *
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
fun rememberPictureInPictureButtonState(): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val pictureInPictureButtonState = remember(activity) {
        PictureInPictureButtonStateBase(activity)
    }

    DisposableEffect(activity) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
    }

    return pictureInPictureButtonState
}

/**
 * Creates a [PictureInPictureButtonState] that is remembered across compositions.
 *
 * [PictureInPictureButtonState.aspectRatio] follows the video played by [player]. To get the best transition to and from
 * Picture-in-Picture, apply [Modifier.pictureInPictureSourceRectHint] to the Composable displaying that video, so that
 * [PictureInPictureButtonState.sourceRectHint] follows it too.
 *
 * @param player The [Player] whose video defines the Picture-in-Picture window.
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
fun rememberPictureInPictureButtonState(player: Player): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val pictureInPictureButtonState = remember(activity, player) {
        PictureInPictureButtonStateBase(activity, player)
    }

    DisposableEffect(pictureInPictureButtonState) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
    }

    return pictureInPictureButtonState
}

/**
 * Creates a [PictureInPictureButtonState] that is remembered across compositions.
 *
 * [pictureInPictureParamsProvider] has full control over the Picture-in-Picture parameters: [PictureInPictureButtonState.aspectRatio] and
 * [PictureInPictureButtonState.sourceRectHint] are ignored by the returned state.
 *
 * @param pictureInPictureParamsProvider A provider to get the parameters to use when entering Picture-in-Picture mode.
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun rememberPictureInPictureButtonState(pictureInPictureParamsProvider: () -> PictureInPictureParams): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val pictureInPictureParamsProvider by rememberUpdatedState(pictureInPictureParamsProvider)
    val pictureInPictureButtonState = remember(activity, pictureInPictureParamsProvider) {
        PictureInPictureButtonStateApi26(activity, pictureInPictureParamsProvider)
    }

    DisposableEffect(activity, pictureInPictureParamsProvider) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
    }

    return pictureInPictureButtonState
}

/**
 * Reports the bounds of the video displayed on screen to [state], so that the system can animate the transition to and from
 * Picture-in-Picture from the video itself.
 *
 * Apply this modifier to the Composable drawing the video, and not to its container, so that the reported bounds don't include the
 * letterboxing around the video.
 *
 * @param state The [PictureInPictureButtonState] to keep up-to-date.
 */
fun Modifier.pictureInPictureSourceRectHint(state: PictureInPictureButtonState): Modifier {
    return onGloballyPositioned { layoutCoordinates ->
        state.sourceRectHint = layoutCoordinates.boundsInWindow()
            .takeIf { !it.isEmpty }
            ?.roundToIntRect()
            ?.let { Rect(it.left, it.top, it.right, it.bottom) }
    }
}

/**
 * State that holds all interactions to correctly deal with a UI component representing a Picture-in-Picture button.
 */
interface PictureInPictureButtonState {
    /**
     * Whether Picture-in-Picture is available.
     */
    val isEnabled: Boolean

    /**
     * Whether the [Activity] is currently in Picture-in-Picture mode.
     */
    val isInPictureInPicture: Boolean

    /**
     * The aspect ratio of the Picture-in-Picture window, coerced to the range supported by the system.
     */
    var aspectRatio: Rational

    /**
     * The area of the window showing the video, used by the system to animate the transition to and from Picture-in-Picture, or `null` when
     * it is unknown.
     */
    var sourceRectHint: Rect?

    /**
     * Enter Picture-in-Picture mode.
     */
    fun onClick()

    /**
     * Start observing the [Activity]'s [PictureInPictureModeChangedInfo] events.
     */
    fun startObserving()

    /**
     * Stop observing the [Activity]'s [PictureInPictureModeChangedInfo] events.
     */
    fun stopObserving()
}

private open class PictureInPictureButtonStateBase(
    activity: ComponentActivity,
    private val player: Player? = null,
) : PictureInPictureButtonState {
    private val activityRef = WeakReference(activity)
    private val pictureInPictureObserver = Consumer<PictureInPictureModeChangedInfo> { changedInfo ->
        isInPictureInPicture = changedInfo.isInPictureInPictureMode
    }
    private val videoSizeObserver = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            aspectRatio = videoSize.toPictureInPictureAspectRatio()
        }
    }

    protected val activity: ComponentActivity?
        get() = activityRef.get()

    override val isEnabled by lazy {
        activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    final override var isInPictureInPicture by mutableStateOf(activity.isInPictureInPictureMode)
        private set

    final override var aspectRatio: Rational = player?.videoSize?.toPictureInPictureAspectRatio() ?: RATIONAL_ONE

    final override var sourceRectHint: Rect? = null

    override fun onClick() {
        activity?.enterPictureInPictureMode(
            PictureInPictureParamsCompat.Builder()
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(sourceRectHint)
                .build()
        )
    }

    override fun startObserving() {
        activity?.addOnPictureInPictureModeChangedListener(pictureInPictureObserver)
        player?.addListener(videoSizeObserver)
    }

    override fun stopObserving() {
        activity?.removeOnPictureInPictureModeChangedListener(pictureInPictureObserver)
        player?.removeListener(videoSizeObserver)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private class PictureInPictureButtonStateApi26(
    activity: ComponentActivity,
    private val pictureInPictureParamsProvider: () -> PictureInPictureParams,
) : PictureInPictureButtonStateBase(activity) {
    override fun onClick() {
        activity?.enterPictureInPictureMode(pictureInPictureParamsProvider())
    }
}

/**
 * Converts this [VideoSize] into an aspect ratio that [Activity.enterPictureInPictureMode] accepts. Aspect ratios that are too extreme are
 * coerced, as the system rejects them.
 */
private fun VideoSize.toPictureInPictureAspectRatio(): Rational {
    val aspectRatio = toRational()

    return when {
        !aspectRatio.isFinite || aspectRatio.isZero -> RATIONAL_ONE
        aspectRatio < MinAspectRatio -> MinAspectRatio
        aspectRatio > MaxAspectRatio -> MaxAspectRatio
        else -> aspectRatio
    }
}
