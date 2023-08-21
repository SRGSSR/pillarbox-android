/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles

/**
 * Track selection settings
 *
 * @param listTracksGroup List of tracks.
 * @param disabled track type is disabled.
 * @param onTrackSelection Action handler.
 * @receiver
 */
@Composable
fun TrackSelectionSettings(
    listTracksGroup: List<Tracks.Group>,
    disabled: Boolean,
    onTrackSelection: (TrackSelectionAction) -> Unit
) {
    val itemModifier = Modifier.fillMaxWidth()
    LazyColumn {
        item {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable { onTrackSelection(TrackSelectionAction.Default) },
                headlineContent = {
                    Text(
                        text = "Reset to default"
                    )
                }
            )
            Divider()
        }
        item {
            SettingsOption(modifier = itemModifier, selected = disabled, onClick = {
                onTrackSelection(TrackSelectionAction.Disable)
            }, content = {
                    Text(text = "Disabled")
                })
            Divider()
        }
        for (group in listTracksGroup) {
            items(group.length) { trackIndex ->
                val format = group.getTrackFormat(trackIndex)
                SettingsOption(modifier = itemModifier, selected = group.isTrackSelected(trackIndex), onClick = {
                    onTrackSelection(TrackSelectionAction.Selection(trackIndex = trackIndex, format = format, group = group))
                }, content = {
                        when (group.type) {
                            C.TRACK_TYPE_AUDIO -> {
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
                    })
            }
            item {
                Divider()
            }
        }
    }
}

@Preview
@Composable
private fun TextTrackSelectionPreview() {
    // Track are group by language.
    val textTrackFR1 = Format.Builder()
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
        Tracks.Group(TrackGroup("fr", textTrackFR1), false, arrayOf(C.FORMAT_HANDLED).toIntArray(), arrayOf(true).toBooleanArray()),
        Tracks.Group(TrackGroup("en", textTrackEn1), false, arrayOf(C.FORMAT_HANDLED).toIntArray(), arrayOf(false).toBooleanArray())
    )
    PillarboxTheme() {
        TrackSelectionSettings(dummyListTrack, disabled = false, onTrackSelection = {})
    }
}

/**
 * Track selection action
 */
sealed interface TrackSelectionAction {
    /**
     * Disable
     */
    data object Disable : TrackSelectionAction

    /**
     * Automatic
     */
    data object Default : TrackSelectionAction

    /**
     * Selection
     *
     * @property trackIndex Track index in [group].
     * @property format Track format.
     * @property group Group where belong [format]
     */
    data class Selection(val trackIndex: Int, val format: Format, val group: Tracks.Group) : TrackSelectionAction
}
