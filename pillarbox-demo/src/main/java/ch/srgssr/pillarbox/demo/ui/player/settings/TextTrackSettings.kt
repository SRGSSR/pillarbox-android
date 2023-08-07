/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Track selection settings
 *
 * @param listTracksGroup List of tracks.
 * @param disabled track type is disabled.
 * @param default track type is in default mode (no overrides).
 * @param onTrackSelection Action handler.
 * @receiver
 */
@Composable
fun TextTrackSettings(
    listTracksGroup: List<Tracks.Group>,
    disabled: Boolean,
    default: Boolean,
    onTrackSelection: (SubtitleAction) -> Unit
) {
    val itemModifier = Modifier.fillMaxWidth()
    LazyColumn {
        item {
            SettingsOption(modifier = itemModifier, selected = disabled, onClick = {
                onTrackSelection(SubtitleAction.Disable)
            }) {
                Text(text = "Disable")
            }
            Divider()
        }
        item {
            SettingsOption(modifier = itemModifier, selected = default && !disabled, onClick = {
                onTrackSelection(SubtitleAction.Automatic)
            }) {
                Text(text = "Automatic")
            }
            Divider()
        }
        for (group in listTracksGroup) {
            items(group.length) { trackIndex ->
                val format = group.getTrackFormat(trackIndex)
                SettingsOption(modifier = itemModifier, selected = group.isTrackSelected(trackIndex), onClick = {
                    onTrackSelection(SubtitleAction.Selection(trackIndex = trackIndex, format = format, group = group))
                }) {
                    Text(text = "${format.label} / ${format.language}")
                }
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
    val trackSelectionParameters = TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT
    PillarboxTheme() {
        TextTrackSettings(dummyListTrack, disabled = false, default = false, onTrackSelection = {})
    }
}

/**
 * Subtitle action
 */
sealed interface SubtitleAction {
    /**
     * Disable
     */
    data object Disable : SubtitleAction

    /**
     * Automatic
     */
    data object Automatic : SubtitleAction

    /**
     * Selection
     *
     * @property trackIndex Track index in [group].
     * @property format Track format.
     * @property group Group where belong [format]
     */
    data class Selection(val trackIndex: Int, val format: Format, val group: Tracks.Group) : SubtitleAction
}
