/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.PlaybackSpeedSetting
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Playback speed settings
 *
 * @param playbackSpeeds The list of possible speeds.
 * @param modifier The [Modifier] to layout the view.
 * @param onSpeedSelected Called when a speed is clicked.
 * @receiver
 */
@Composable
fun PlaybackSpeedSettings(
    playbackSpeeds: List<PlaybackSpeedSetting>,
    modifier: Modifier = Modifier,
    onSpeedSelected: (PlaybackSpeedSetting) -> Unit,
) {
    LazyColumn(modifier) {
        itemsIndexed(items = playbackSpeeds) { index, playbackSpeed ->
            SettingsOptionItem(
                title = playbackSpeed.speed,
                enabled = playbackSpeed.isSelected,
                modifier = Modifier.toggleable(playbackSpeed.isSelected) {
                    onSpeedSelected(playbackSpeed)
                }
            )

            if (index < playbackSpeeds.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SettingsOptionItem(title: String, enabled: Boolean, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(text = title) },
        trailingContent = {
            if (enabled) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "enabled")
            }
        }
    )
}

@Preview
@Composable
private fun PlaybackSpeedSettingPreview() {
    val playbackSpeeds = listOf(
        PlaybackSpeedSetting(
            speed = "0.5×",
            rawSpeed = 0.5f,
            isSelected = false
        ),
        PlaybackSpeedSetting(
            speed = "Normal",
            rawSpeed = 1f,
            isSelected = true
        ),
        PlaybackSpeedSetting(
            speed = "2×",
            rawSpeed = 2f,
            isSelected = false
        )
    )

    PillarboxTheme {
        PlaybackSpeedSettings(
            playbackSpeeds = playbackSpeeds,
            onSpeedSelected = {}
        )
    }
}
