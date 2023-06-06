/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayingControls

/**
 * Demo playback controls
 *
 * @param player The [StatefulPlayer] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param autoHideEnabled To enable or not auto hide of the controls.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 * @param optionClicked action when settings is clicked
 */
@Composable
fun DemoPlaybackControls(
    player: Player,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    autoHideEnabled: Boolean = true,
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
    optionClicked: (() -> Unit)? = null
) {
    if (controlVisible) {
        PlayingControls(
            modifier = modifier,
            player = player,
            autoHideEnabled = autoHideEnabled,
            fullScreenEnabled = fullScreenEnabled,
            fullScreenClicked = fullScreenClicked,
            pictureInPictureClicked = pictureInPictureClicked,
            optionClicked = optionClicked
        )
    }
}
