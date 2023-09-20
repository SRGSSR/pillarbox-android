/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.ToggleView
import ch.srgssr.pillarbox.ui.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.playbackStateAsState
import ch.srgssr.pillarbox.ui.rememberToggleState

/**
 * Playing controls
 *
 * Playing controls
 * The view to display when something is ready to play.
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param autoHideEnabled To enable or not auto hide of the controls.
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
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
    optionClicked: (() -> Unit)? = null
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val toggleState = rememberToggleState(
        player = player,
        visible = controlVisible,
        autoHideEnabled = autoHideEnabled,
        interactionSource = interactionSource
    )
    Box(
        modifier = modifier.clickable(role = Role.Switch, onClickLabel = "Toggle controls", onClick = toggleState::toggleVisible)
    ) {
        if (player.playbackStateAsState() == Player.STATE_BUFFERING) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        ToggleView(
            modifier = Modifier.fillMaxSize(),
            toggleState = toggleState
        ) {
            val mediaMetadata = player.currentMediaMetadataAsState()
            Box(modifier = Modifier.matchParentSize()) {
                Text(modifier = Modifier.align(Alignment.TopStart), text = mediaMetadata.title.toString(), color = Color.Gray)
                PlayerPlaybackRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    player = player
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
                        player = player,
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
