/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.isPlayingAsState
import ch.srgssr.pillarbox.ui.playerErrorAsState

/**
 * Simple player view
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 * @param optionClicked action when settings is clicked
 */
@Composable
fun SimplePlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
    optionClicked: (() -> Unit)? = null
) {
    val playerError = player.playerErrorAsState()
    if (playerError != null) {
        PlayerError(modifier = modifier, playerError = playerError, onRetry = player::prepare)
        return
    }
    if (!player.hasMediaItemsAsState()) {
        Surface(modifier = modifier, color = Color.Black) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    text = "No content",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        return
    }
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
    LocalView.current.keepScreenOn = player.isPlayingAsState()
    DemoPlayerSurface(
        modifier = surfaceModifier,
        player = player,
        scaleMode = if (fullScreenEnabled) pinchScaleMode else ScaleMode.Fit
    ) {
        DemoPlaybackControls(
            modifier = Modifier
                .matchParentSize(),
            player = player,
            controlVisible = controlVisible,
            autoHideEnabled = true,
            fullScreenEnabled = fullScreenEnabled,
            fullScreenClicked = fullScreenClicked,
            pictureInPictureClicked = pictureInPictureClicked,
            optionClicked = optionClicked
        )
    }
}
