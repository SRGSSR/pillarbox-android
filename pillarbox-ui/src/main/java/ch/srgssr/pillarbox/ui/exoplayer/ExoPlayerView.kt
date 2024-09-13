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
 * Composable PlayerView provided by Media3 library
 *
 * @param player The player to attach to the [PlayerView]
 * @param modifier The modifier to be applied to the layout.
 * @param useController true to display controls
 * @param controllerAutoShow [PlayerView.setControllerAutoShow]
 * @param showNextButton [PlayerView.setShowNextButton]
 * @param showPreviousButton [PlayerView.setShowPreviousButton]
 * @param showBuffering [PlayerView.setShowBuffering]
 * @param resizeMode The resize mode for [PlayerView]
 * @param errorMessageProvider The errorMessageProvider
 * @param fullScreenListener [PlayerView.setFullscreenButtonClickListener]
 * @param controllerVisibilityListener [PlayerView.setControllerVisibilityListener]
 * @param shutterBackgroundColor [PlayerView.setShutterBackgroundColor]
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
    @ColorInt shutterBackgroundColor: Int = 0
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
