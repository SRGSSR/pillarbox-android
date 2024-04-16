/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.TracksSettingItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.Track
import ch.srgssr.pillarbox.player.tracks.VideoQualityTrack

/**
 * Track selection settings
 *
 * @param tracksSetting List of tracks.
 * @param modifier The [Modifier] to apply to this screen.
 * @param onResetClick The action to perform when clicking on the reset button.
 * @param onDisabledClick The action to perform when clicking on the disable button.
 * @param onTrackClick The action to perform when clicking on a track.
 */
@Composable
fun TrackSelectionSettings(
    tracksSetting: TracksSettingItem,
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit,
    onDisabledClick: () -> Unit,
    onTrackClick: (track: Track) -> Unit,
) {
    val itemModifier = Modifier.fillMaxWidth()
    LazyColumn(modifier = modifier) {
        item {
            ListItem(
                modifier = itemModifier
                    .minimumInteractiveComponentSize()
                    .clickable { onResetClick() },
                headlineContent = {
                    Text(
                        text = stringResource(R.string.reset_to_default)
                    )
                }
            )
            HorizontalDivider()
        }
        item {
            SettingsOption(
                modifier = itemModifier,
                selected = tracksSetting.disabled,
                onClick = onDisabledClick,
                content = {
                    Text(text = stringResource(R.string.disabled))
                }
            )
            HorizontalDivider()
        }
        items(tracksSetting.tracks) { track ->
            SettingsOption(
                modifier = itemModifier,
                selected = track.isSelected,
                onClick = {
                    onTrackClick(track)
                },
                content = {
                    val format = track.format
                    when (track) {
                        is AudioTrack -> {
                            val str = StringBuilder()
                            str.append(format.displayName)
                            if (format.bitrate > Format.NO_VALUE) {
                                str.append(" @${format.bitrate} bit/sec")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = str.toString())
                                if (format.hasAccessibilityRoles()) {
                                    Icon(imageVector = Icons.Filled.HearingDisabled, contentDescription = "AD")
                                }
                            }
                        }

                        is VideoQualityTrack -> {
                            Text(text = "${format.height}p")
                        }

                        else -> {
                            if (format.hasAccessibilityRoles()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = format.displayName)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(imageVector = Icons.Default.HearingDisabled, contentDescription = "Hearing disabled")
                                }
                            } else {
                                Text(text = format.displayName)
                            }
                        }
                    }
                }
            )
        }
        item {
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun TextTrackSelectionPreview() {
    // Track are group by language.
    val textTrackFr1 = Format.Builder()
        .setLabel("FR1")
        .setId("subtitle:0")
        .setLanguage("fr")
        .build()
    val textTrackEn1 = Format.Builder()
        .setLabel("EN1")
        .setId("subtitle:1")
        .setLanguage("en")
        .build()
    val dummyListTrack = listOf(
        TextTrack(
            group = Tracks.Group(TrackGroup("fr", textTrackFr1), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
            groupIndex = 0,
            trackIndexInGroup = 0,
        ),
        TextTrack(
            group = Tracks.Group(TrackGroup("en", textTrackEn1), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(false)),
            groupIndex = 0,
            trackIndexInGroup = 0,
        ),
    )
    PillarboxTheme {
        TrackSelectionSettings(
            tracksSetting = TracksSettingItem(
                title = stringResource(R.string.subtitles),
                tracks = dummyListTrack,
                disabled = false
            ),
            onResetClick = {},
            onDisabledClick = {},
            onTrackClick = {},
        )
    }
}
