/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Default speeds
 */
val DefaultSpeeds = listOf(
    0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f,
)

/**
 * Default speed provider
 */
val DefaultSpeedLabelProvider: (Float) -> String = { speed ->
    if (speed == 1.0f) {
        "Normal"
    } else {
        "$speed"
    }
}

/**
 * Playback speed settings
 *
 * @param currentSpeed The current speed should be inside [speeds].
 * @param onSpeedSelected Called when a speed is clicked.
 * @param modifier The Modifier to layout the view.
 * @param speedLabelProvider Create a String from a speed.
 * @param speeds List of possible speeds.
 * @receiver
 */
@Composable
fun PlaybackSpeedSettings(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    speedLabelProvider: (Float) -> String = DefaultSpeedLabelProvider,
    speeds: List<Float> = DefaultSpeeds
) {

    LazyColumn(modifier) {
        items(items = speeds) { speed ->
            val enabled = speed == currentSpeed
            SettingsOptionItem(
                title = speedLabelProvider(speed),
                enabled = enabled,
                modifier = Modifier.toggleable(enabled) {
                    onSpeedSelected(speed)
                }
            )
            Divider()
        }
    }
}

@Composable
private fun SettingsOptionItem(title: String, enabled: Boolean, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(text = title) },
        trailingContent =
        {
            if (enabled) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "enabled")
            }
        }
    )
}

@Preview
@Composable
private fun PlaybackSpeedSettingPreview() {
    val speeds = listOf(0.5f, 1.0f, 2.0f)
    PillarboxTheme {
        Column() {
            PlaybackSpeedSettings(
                currentSpeed = speeds[0],
                speeds = speeds,
                onSpeedSelected = {}
            )
        }
    }
}
