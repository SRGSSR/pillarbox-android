/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayingControls
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.rememberPlayerState

/**
 * Demo playback controls
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
fun DemoPlaybackControls(
    player: Player,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    playerState: PlayerState = rememberPlayerState(player = player),
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
) {
    if (controlVisible) {
        PlayingControls(
            modifier = modifier,
            player = player,
            playerState = playerState,
            fullScreenEnabled = fullScreenEnabled,
            fullScreenClicked = fullScreenClicked,
            pictureInPictureClicked = pictureInPictureClicked
        )
    }
}
