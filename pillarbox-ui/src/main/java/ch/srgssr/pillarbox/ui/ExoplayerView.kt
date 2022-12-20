/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ShowBuffering
import ch.srg.pillarbox.core.business.SRGErrorMessageProvider

/**
 * PlayerView provided by Media3 library
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
 * @param keepScreenOn true to keep screen on, regardless [player] state
 */
@Composable
fun ExoplayerView(
    player: Player?,
    modifier: Modifier = Modifier,
    useController: Boolean = true,
    controllerAutoShow: Boolean = true,
    showNextButton: Boolean = true,
    showPreviousButton: Boolean = true,
    showBuffering: @ShowBuffering Int = PlayerView.SHOW_BUFFERING_NEVER,
    resizeMode: @AspectRatioFrameLayout.ResizeMode Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    errorMessageProvider: ErrorMessageProvider<PlaybackException>? = SRGErrorMessageProvider(),
    fullScreenListener: PlayerView.FullscreenButtonClickListener? = null,
    controllerVisibilityListener: PlayerView.ControllerVisibilityListener? = null,
    @ColorInt shutterBackgroundColor: Int = 0,
    keepScreenOn: Boolean = false
) {
    ScreenOnKeeper(keepScreenOn = keepScreenOn)
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
        }
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
    val playerView = PlayerView(context)
    val lifecycleObserver = rememberPlayerViewLifecycleObserver(playerView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            playerView.player = null
        }
    }
    return playerView
}

@Composable
private fun rememberPlayerViewLifecycleObserver(playerView: PlayerView): LifecycleEventObserver =
    remember(playerView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> playerView.onResume()
                Lifecycle.Event.ON_PAUSE -> playerView.onPause()
                else -> {}
            }
        }
    }
