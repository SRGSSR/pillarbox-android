/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.exoplayer

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ShowBuffering

/**
 * A Composable function that displays an ExoPlayer [PlayerView].
 *
 * @param player The [Player] instance to be attached to the [PlayerView].
 * @param modifier The [Modifier] to apply to this layout.
 * @param useController Whether to display playback controls.
 * @param controllerAutoShow Whether the controls should be shown automatically when the playback starts, pauses, ends or fails.
 * @param showNextButton Whether to display the "next" button in the controller.
 * @param showPreviousButton Whether to display the "previous" button in the controller.
 * @param showBuffering Specifies when to display the buffering indicator.
 * @param resizeMode Specifies how the video content should be resized to fit the [PlayerView].
 * @param errorMessageProvider An optional [ErrorMessageProvider] to customize error messages displayed during playback failures.
 * @param fullScreenListener An optional [PlayerView.FullscreenButtonClickListener] to handle clicks on the fullscreen button.
 * @param controllerVisibilityListener An optional [PlayerView.ControllerVisibilityListener] to receive callbacks when the controller's visibility
 * changes.
 * @param shutterBackgroundColor The color of the shutter (background) when the video is not playing.
 * @param setupView An optional callback allowing customization of the underlying [PlayerView].
 *
 * @see PlayerView.setUseController
 * @see PlayerView.setControllerAutoShow
 * @see PlayerView.setShowNextButton
 * @see PlayerView.setShowPreviousButton
 * @see PlayerView.setShowShuffleButton
 * @see PlayerView.setShowSubtitleButton
 * @see PlayerView.setShowBuffering
 * @see PlayerView.setResizeMode
 * @see PlayerView.setErrorMessageProvider
 * @see PlayerView.setFullscreenButtonClickListener
 * @see PlayerView.setControllerVisibilityListener
 * @see PlayerView.setShutterBackgroundColor
 */
@Suppress("DEPRECATION")
@Composable
fun ExoPlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    useController: Boolean = true,
    controllerAutoShow: Boolean = true,
    showNextButton: Boolean = true,
    showPreviousButton: Boolean = true,
    showBuffering: @ShowBuffering Int = PlayerView.SHOW_BUFFERING_NEVER,
    resizeMode: @AspectRatioFrameLayout.ResizeMode Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    errorMessageProvider: ErrorMessageProvider<PlaybackException>? = null,
    fullScreenListener: PlayerView.FullscreenButtonClickListener? = null,
    controllerVisibilityListener: PlayerView.ControllerVisibilityListener? = null,
    @ColorInt shutterBackgroundColor: Int = 0,
    setupView: PlayerView.() -> Unit = {},
) {
    val playerView = rememberPlayerView()
    AndroidView(
        modifier = modifier,
        factory = { playerView },
        update = { view ->
            view.resizeMode = resizeMode
            view.setShowBuffering(showBuffering)
            view.setErrorMessageProvider(errorMessageProvider)
            view.controllerAutoShow = controllerAutoShow
            view.useController = useController
            view.setFullscreenButtonClickListener(fullScreenListener)
            if (fullScreenListener == null) {
                // We use depreciated version to hide fullscreen button if fullScreenListener is null to disable full screen button
                view.setControllerOnFullScreenModeChangedListener(null)
            }
            view.setControllerVisibilityListener(controllerVisibilityListener)
            view.setShowNextButton(showNextButton)
            view.setShowPreviousButton(showPreviousButton)
            view.setShutterBackgroundColor(shutterBackgroundColor)
            view.player = player
            view.setupView()
        },
        onRelease = { view ->
            view.player = null
        },
        onReset = NoOpUpdate
    )
}

/**
 * Remember player view
 *
 * Create a [PlayerView] that is Lifecyle aware.
 * OnDispose remove player from the view.
 *
 * @return the [PlayerView]
 */
@Composable
private fun rememberPlayerView(): PlayerView {
    val context = LocalContext.current
    val playerView = remember { PlayerView(context) }

    LifecycleResumeEffect(playerView) {
        playerView.onResume()

        onPauseOrDispose {
            playerView.onPause()
        }
    }
    return playerView
}
