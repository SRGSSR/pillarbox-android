/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerBottomToolbar
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerTimeSlider
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.ToggleableBox
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface2
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState

/**
 * Simple player view
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param controlsVisible The control visibility.
 * @param controlsToggleable The controls are toggleable.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 * @param optionClicked action when settings is clicked
 */
@Composable
fun SimplePlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    controlsVisible: Boolean = true,
    controlsToggleable: Boolean = true,
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

    val scalableModifier = if (fullScreenEnabled) {
        modifier.then(
            Modifier.pointerInput(pinchScaleMode) {
                var lastZoomValue = 1.0f
                detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                    lastZoomValue *= zoom
                    pinchScaleMode = if (lastZoomValue < 1.0f) ScaleMode.Fit else ScaleMode.Crop
                }
            }
        )
    } else {
        modifier
    }
    val isPlaying = player.isPlayingAsState()
    LocalView.current.keepScreenOn = isPlaying
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isDragged = interactionSource.collectIsDraggedAsState().value
    val visibilityState = rememberDelayedVisibilityState(
        player = player,
        autoHideEnabled = !isDragged,
        visible = controlsVisible
    )

    ToggleableBox(
        modifier = scalableModifier,
        toggleable = controlsToggleable,
        visibilityState = visibilityState,
        toggleableContent = {
            val mediaMetadata = player.currentMediaMetadataAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(color = Color.Black.copy(0.5f))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.align(Alignment.TopStart),
                    text = mediaMetadata.title.toString(), color = Color.Gray
                )
                PlayerPlaybackRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    player = player
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    PlayerTimeSlider(
                        modifier = Modifier,
                        player = player,
                        interactionSource = interactionSource
                    )
                    PlayerBottomToolbar(
                        modifier = Modifier
                            .fillMaxWidth(),
                        fullScreenEnabled = fullScreenEnabled,
                        fullScreenClicked = fullScreenClicked,
                        pictureInPictureClicked = pictureInPictureClicked,
                        optionClicked = optionClicked
                    )
                }
            }
        }
    ) {
        val isBuffering = player.playbackStateAsState() == Player.STATE_BUFFERING
        PlayerSurface2(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            player = player,
            scaleMode = if (fullScreenEnabled) pinchScaleMode else ScaleMode.Fit
        ) {
            if (isBuffering) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
            }
            ExoPlayerSubtitleView(player = player)
        }
    }
}
