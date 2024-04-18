/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingItemOptions
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.tracks.TextTrack

/**
 * Selection setting options.
 *
 * @param T The type of option.
 * @param tracksSetting List of tracks.
 * @param modifier The [Modifier] to apply to this screen.
 * @param onResetClick The action to perform when clicking on the reset button.
 * @param onDisabledClick The action to perform when clicking on the disable button.
 * @param itemContent The content to display for the provided [item][T].
 */
@Composable
fun <T> SelectionSettingOptions(
    tracksSetting: SettingItemOptions<T>,
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit,
    onDisabledClick: () -> Unit,
    itemContent: @Composable (item: T) -> Unit,
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
        items(tracksSetting.items) { item ->
            itemContent(item)
        }
        item {
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun SelectionSettingOptionsPreview() {
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
        SelectionSettingOptions(
            tracksSetting = SettingItemOptions(
                title = stringResource(R.string.subtitles),
                items = dummyListTrack,
                disabled = false
            ),
            itemContent = { item ->
                Text(text = item.format.displayName)
            },
            onResetClick = {},
            onDisabledClick = {},
        )
    }
}
