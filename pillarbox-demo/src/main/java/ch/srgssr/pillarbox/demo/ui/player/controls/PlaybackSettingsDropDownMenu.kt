/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.playbackSpeedAsState

private val speeds = mapOf(
    Pair("0.25", 0.25f),
    Pair("0.5", 0.5f),
    Pair("0.75", 0.75f),
    Pair("Normal", 1.0f),
    Pair("1.25", 1.25f),
    Pair("1.5", 1.5f),
    Pair("2", 2.0f),
)

/**
 * Playback settings drop down menu
 *
 * @param player The player to display the settings.
 * @param expanded display or not the menu
 * @param onDismissed action when dismissing the menu.
 */
@Composable
fun PlaybackSettingsDropDownMenu(
    player: Player,
    expanded: Boolean = false,
    onDismissed: () -> Unit = {},
) {
    val currentPlaybackSpeed by player.playbackSpeedAsState()
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissed) {
        Text(
            text = "Playbacks options",
            modifier = Modifier.padding(horizontal = MaterialTheme.paddings.baseline)
        )
        Divider()
        for (speed in speeds) {
            val selected = speed.value == currentPlaybackSpeed
            DropdownMenuItem(
                text = {
                    Text(speed.key)
                    if (selected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Selected")
                    }
                },
                enabled = !selected,
                onClick = {
                    player.setPlaybackSpeed(speed.value)
                    onDismissed()
                }
            )
            Divider()
        }
    }
}
