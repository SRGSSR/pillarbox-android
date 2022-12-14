/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import ch.srg.pillarbox.core.business.SRGErrorMessageProvider

/**
 * PlayerView provided by Media3 library
 *
 * @param player The player to attach to the [PlayerView]
 * @param modifier The modifier to be applied to the layout.
 * @param useController true to display controls
 * @param resizeMode The resize mode for [PlayerView]
 * @param errorMessageProvider The errorMessageProvider
 * @param fullScreenListener [PlayerView.setFullscreenButtonClickListener]
 * @param controllerVisibilityListener [PlayerView.setControllerVisibilityListener]
 */
@Composable
fun ExoplayerView(
    player: Player?,
    modifier: Modifier = Modifier,
    useController: Boolean = true,
    resizeMode: @AspectRatioFrameLayout.ResizeMode Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    errorMessageProvider: ErrorMessageProvider<PlaybackException>? = SRGErrorMessageProvider(),
    fullScreenListener: PlayerView.FullscreenButtonClickListener? = null,
    controllerVisibilityListener: PlayerView.ControllerVisibilityListener? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).also { view ->
                // Seems not working with Compose
                // view.keepScreenOn = true
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        update = { view ->
            view.player = player
            view.resizeMode = resizeMode
            view.setErrorMessageProvider(errorMessageProvider)
            view.controllerAutoShow = useController
            view.useController = useController
            view.setFullscreenButtonClickListener(fullScreenListener)
            if (fullScreenListener == null) {
                // We use depreciated version to hide fullscreen button if fullScreenListener is null to disable full screen button
                view.setControllerOnFullScreenModeChangedListener(null)
            }

            view.setControllerVisibilityListener(controllerVisibilityListener)
        }
    )
}
