/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerView
import ch.srgssr.pillarbox.player.StatefulPlayer

/**
 * Playlist player view display a SimplePlayerView with a Playlist management view.
 *
 * @param player The [StatefulPlayer] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param controlVisible The control visibility.
 * @param fullScreenEnabled The fullscreen state.
 * @param fullScreenClicked The fullscreen button action. If null no button.
 * @param pictureInPictureClicked The picture in picture button action. If null no button.
 * @param optionClicked action when settings is clicked
 */
@Composable
fun PlaylistPlayerView(
    player: StatefulPlayer,
    modifier: Modifier = Modifier,
    controlVisible: Boolean = true,
    fullScreenEnabled: Boolean = false,
    fullScreenClicked: ((Boolean) -> Unit)? = null,
    pictureInPictureClicked: (() -> Unit)? = null,
    optionClicked: (() -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current
    val fullScreenMode = fullScreenEnabled || configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val playerModifier = if (fullScreenMode) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(AspectRatio)
        }
        SimplePlayerView(
            modifier = playerModifier,
            player = player,
            controlVisible = controlVisible,
            fullScreenEnabled = fullScreenEnabled,
            fullScreenClicked = fullScreenClicked,
            pictureInPictureClicked = pictureInPictureClicked,
            optionClicked = optionClicked
        )
        if (!fullScreenMode) {
            PlaylistActionsView(modifier = Modifier.fillMaxWidth(), player = player)
            CurrentPlaylistView(modifier = Modifier.fillMaxWidth(), player = player)
        }
    }
}

private const val AspectRatio = 16 / 9f
