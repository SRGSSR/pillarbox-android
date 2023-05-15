/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.ui.playbackSpeed
import ch.srgssr.pillarbox.ui.rememberPlayerState

private val speeds = mapOf(
    Pair("0.25", 0.25f),
    Pair("0.5", 0.5f),
    Pair("0.75", 0.75f),
    Pair("normal", 1.0f),
    Pair("1.25", 1.25f),
    Pair("1.5", 1.5f),
    Pair("2", 2.0f),
)

/**
 * Playback settings drop down menu
 *
 * @param player
 * @param playerState
 * @param expanded display or not the menu
 * @param onDismissed action when dismissing the menu.
 */
@Composable
fun PlaybackSettingsDropDownMenu(
    player: Player,
    playerState: PlayerState = rememberPlayerState(player = player),
    expanded: Boolean = false,
    onDismissed: () -> Unit = {},
) {
    val currentPlaybackSpeed = playerState.playbackSpeed()
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissed) {
        Text(text = "Playbacks options")
        Divider()
        for (speed in speeds) {
            val selected = speed.value == currentPlaybackSpeed
            DropdownMenuItem(enabled = !selected, onClick = {
                player.setPlaybackSpeed(speed.value)
                onDismissed()
            }) {
                Text(speed.key)
                if (selected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected")
                }
            }
            Divider()
        }
    }
}
