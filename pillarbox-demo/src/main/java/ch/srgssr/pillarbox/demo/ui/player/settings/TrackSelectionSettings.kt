/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.TracksSettingItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles
import ch.srgssr.pillarbox.player.extension.isForced
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.Track
import ch.srgssr.pillarbox.player.tracks.VideoTrack

/**
 * Track selection settings
 *
 * @param tracksSetting List of tracks.
 * @param modifier The [Modifier] to apply to this screen.
 * @param onResetClick The action to perform when clicking on the reset button.
 * @param onDisabledClick The action to perform when clicking on the "Disabled" button.
 * @param onTrackClick The action to perform when clicking on a track.
 */
@Composable
fun TrackSelectionSettings(
    tracksSetting: TracksSettingItem,
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit,
    onDisabledClick: () -> Unit,
    onTrackClick: (track: Track) -> Unit
) {
    val itemModifier = Modifier.fillMaxWidth()
    LazyColumn(
        modifier = modifier.semantics {
            // Adding 2 for the "Reset to default" and "Disabled" options
            collectionInfo = CollectionInfo(rowCount = tracksSetting.tracks.size + 2, columnCount = 1)
        },
    ) {
        item {
            ListItem(
                modifier = itemModifier
                    .minimumInteractiveComponentSize()
                    .clickable { onResetClick() }
                    .semantics {
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = 0,
                            rowSpan = 1,
                            columnIndex = 1,
                            columnSpan = 1,
                        )
                    },
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
                modifier = itemModifier
                    .semantics {
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = 1,
                            rowSpan = 1,
                            columnIndex = 1,
                            columnSpan = 1,
                        )
                    },
                selected = tracksSetting.disabled,
                onClick = onDisabledClick,
                content = {
                    Text(text = stringResource(R.string.disabled))
                }
            )
            HorizontalDivider()
        }
        itemsIndexed(tracksSetting.tracks) { index, track ->
            val format = track.format
            SettingsOption(
                modifier = itemModifier
                    .semantics {
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = index + 2,
                            rowSpan = 1,
                            columnIndex = 1,
                            columnSpan = 1,
                        )
                    },
                selected = track.isSelected,
                enabled = track.isSupported && !format.isForced(),
                onClick = {
                    onTrackClick(track)
                },
                content = {
                    when (track) {
                        is AudioTrack -> {
                            val text = buildString {
                                append(format.displayName)

                                if (format.bitrate > Format.NO_VALUE) {
                                    append(" @%1\$.2f Mbps".format(format.bitrate / 1_000_000f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = text)
                                if (format.hasAccessibilityRoles()) {
                                    Icon(
                                        imageVector = Icons.Filled.HearingDisabled,
                                        contentDescription = "AD",
                                        modifier = Modifier.padding(start = MaterialTheme.paddings.small),
                                    )
                                }
                            }
                        }

                        is VideoTrack -> {
                            val text = buildString {
                                append(format.width)
                                append("×")
                                append(format.height)

                                if (format.bitrate > Format.NO_VALUE) {
                                    append(" @%1\$.2f Mbps".format(format.bitrate / 1_000_000f))
                                }
                            }

                            Text(text = text)
                        }

                        else -> {
                            if (format.hasAccessibilityRoles()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = format.displayName)
                                    Icon(
                                        imageVector = Icons.Default.HearingDisabled,
                                        contentDescription = "Hearing disabled",
                                        modifier = Modifier.padding(start = MaterialTheme.paddings.small),
                                    )
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
    // Tracks are group by language.
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
    val dummyListTrack = listOfNotNull(
        Track(
            group = Tracks.Group(TrackGroup("fr", textTrackFr1), false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(true)),
            groupIndex = 0,
            trackIndexInGroup = 0,
        ),
        Track(
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
