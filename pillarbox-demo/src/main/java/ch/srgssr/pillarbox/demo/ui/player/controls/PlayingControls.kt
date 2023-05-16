/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.AnimatedVisibilityAutoHide
import ch.srgssr.pillarbox.ui.isPlaying
import ch.srgssr.pillarbox.ui.playbackState
import ch.srgssr.pillarbox.ui.rememberPlayerState

/**
 * Playing controls
 *
 * Playing controls
 * The view to display when something is ready to play.
 *
 * @param player The [Player] actions occurred.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param autoHideEnabled To enable or not auto hide of the controls.
 * @param playerState The [PlayerState] to observe.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 * @param optionClicked action when settings is clicked
 */
@Composable
fun PlayingControls(
    player: Player,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    autoHideEnabled: Boolean = true,
    playerState: PlayerState = rememberPlayerState(player = player),
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
    optionClicked: (() -> Unit)? = null
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isDragged = interactionSource.collectIsDraggedAsState()
    var toggleControls by remember {
        mutableStateOf(controlVisible)
    }
    val isPlaying = playerState.isPlaying()
    Box(
        modifier = modifier.clickable(role = Role.Switch, onClickLabel = "Toggle controls") { toggleControls = !toggleControls }
    ) {
        if (playerState.playbackState() == Player.STATE_BUFFERING) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
        AnimatedVisibilityAutoHide(
            visible = toggleControls,
            autoHideEnabled = autoHideEnabled && !isDragged.value && isPlaying,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                PlayerPlaybackRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    player = player, playerState = playerState
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.Start
                ) {
                    PlayerTimeSlider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        player = player, playerState = playerState,
                        interactionSource = interactionSource
                    )
                    PlayerBottomToolbar(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                        fullScreenEnabled = fullScreenEnabled,
                        fullScreenClicked = fullScreenClicked,
                        pictureInPictureClicked = pictureInPictureClicked,
                        optionClicked = optionClicked
                    )
                }
            }
        }
    }
}
