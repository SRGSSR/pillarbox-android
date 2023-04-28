/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.rememberPlayerState

/**
 * Simple player view
 *
 * @param player The [Player] actions occurred.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param playerState The [PlayerState] to observe.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 */
@Composable
fun SimplePlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    playerState: PlayerState = rememberPlayerState(player = player),
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
) {
    var pinchScaleMode by remember {
        mutableStateOf(ScaleMode.Fit)
    }
    val surfaceModifier = if (fullScreenEnabled) {
        modifier.then(
            Modifier.pointerInput(pinchScaleMode) {
                var lastZoomValue = 1.0f
                detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                    lastZoomValue *= zoom
                    pinchScaleMode = if (lastZoomValue < 1.0f) ScaleMode.Fit else ScaleMode.Zoom
                }
            }
        )
    } else {
        modifier
    }

    DemoPlayerSurface(
        modifier = surfaceModifier,
        player = player,
        scaleMode = if (fullScreenEnabled) pinchScaleMode else ScaleMode.Fit
    ) {
        DemoPlaybackControls(
            modifier = Modifier.matchParentSize(),
            player = player,
            playerState = playerState,
            controlVisible = controlVisible,
            fullScreenEnabled = fullScreenEnabled,
            fullScreenClicked = fullScreenClicked,
            pictureInPictureClicked = pictureInPictureClicked
        )
    }
}
