/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.playbackSpeedAsState

/**
 * Playback settings content
 *
 * @param player The [Player] actions occurred.
 * @param onDismiss The callback to dismiss the settings.
 */
@Composable
fun ColumnScope.PlaybackSettingsContent(
    player: Player,
    onDismiss: () -> Unit,
) {
    val onDismissState = remember {
        onDismiss
    }
    val currentPlaybackSpeed = player.playbackSpeedAsState()
    var playbackSettingsSelected by remember(currentPlaybackSpeed) {
        mutableStateOf(false)
    }

    if (playbackSettingsSelected) {
        PlaybackSpeedSettings(currentSpeed = currentPlaybackSpeed, onSpeedSelected = {
            player.setPlaybackSpeed(it)
            onDismissState.invoke()
        })
    } else {
        SettingsItem(
            modifier = Modifier.clickable(enabled = true, role = Role.Button) {
                playbackSettingsSelected = true
            },
            title = "Speed", secondaryText = DefaultSpeedLabelProvider(currentPlaybackSpeed), imageVector = Icons.Default.Speed
        )
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
