/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.rememberPlayerState
import ch.srgssr.pillarbox.ui.shuffleModeEnabled

/**
 * Playlist actions view
 *
 * @param player Player to modify
 * @param modifier Modifier
 * @param playerState PlayerState to listen to player changes
 */
@Composable
fun PlaylistActionsView(
    player: Player,
    modifier: Modifier = Modifier,
    playerState: PlayerState = rememberPlayerState(player = player)
) {
    val shuffleModeEnable = playerState.shuffleModeEnabled()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        IconToggleButton(checked = shuffleModeEnable, onCheckedChange = player::setShuffleModeEnabled) {
            if (shuffleModeEnable) {
                Icon(imageVector = Icons.Default.ShuffleOn, contentDescription = "Disable playlist shuffle")
            } else {
                Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Enable playlist shuffle")
            }
        }
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add items to the playlist")
        }
        IconButton(onClick = player::clearMediaItems) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear playlist")
        }
    }
}
