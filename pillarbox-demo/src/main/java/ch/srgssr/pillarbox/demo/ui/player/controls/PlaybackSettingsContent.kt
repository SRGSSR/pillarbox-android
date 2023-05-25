/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.player.canSpeedAndPitch
import ch.srgssr.pillarbox.ui.availableCommands
import ch.srgssr.pillarbox.ui.rememberPlayerState

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
 * Playback settings content
 *
 * @param player The [Player] actions occurred.
 * @param playerState The [PlayerState] to observe.
 * @param onDismiss The callback to dismiss the settings.
 */
@Composable
fun ColumnScope.PlaybackSettingsContent(
    player: Player,
    playerState: PlayerState = rememberPlayerState(player = player),
    onDismiss: () -> Unit,
) {
    val onDismissState = remember {
        onDismiss
    }
    val currentPlaybackSpeed = playerState.playbackSpeed.collectAsState()
    val commands = playerState.availableCommands()
    val currentSpeedLabel = remember {
        derivedStateOf {
            speeds.entries.first { it.value == currentPlaybackSpeed.value }.key
        }
    }
    var playbackSettingsSelected by remember(currentPlaybackSpeed.value) {
        mutableStateOf(false)
    }

    if (playbackSettingsSelected) {
        for (speed in speeds) {
            val enabled = commands.canSpeedAndPitch() && currentSpeedLabel.value == speed.key
            SettingsOptionItem(
                title = speed.key,
                enabled = enabled,
                modifier = Modifier.toggleable(enabled) {
                    player.setPlaybackSpeed(speed.value)
                    onDismissState.invoke()
                }
            )
            Divider()
        }
    } else {
        SettingsItem(
            modifier = Modifier.clickable(enabled = true, role = Role.Button) {
                playbackSettingsSelected = true
            },
            title = "Speed", secondaryText = currentSpeedLabel.value, imageVector = Icons.Default.Speed
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SettingsOptionItem(title: String, enabled: Boolean, modifier: Modifier = Modifier) {
    ListItem(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title)
            if (enabled) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "enabled")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SettingsItem(
    title: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    secondaryText: String? = null
) {
    ListItem(
        modifier = modifier,
        icon = { Icon(imageVector = imageVector, contentDescription = null) },
        secondaryText = secondaryText?.let {
            { Text(text = it) }
        }
    ) {
        Text(text = title)
    }
}
