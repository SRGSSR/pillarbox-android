/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.tv.item.DemoItemView
import ch.srgssr.pillarbox.demo.tv.item.PlaylistHeader

/**
 * Examples home
 *
 * @param modifier
 * @param onItemSelected
 * @receiver
 */
@OptIn(ExperimentalTvFoundationApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun ExamplesHome(
    modifier: Modifier = Modifier,
    onItemSelected: (DemoItem) -> Unit = {},
) {
    val listItems = remember {
        listOf(
            Playlist.StreamUrls,
            Playlist.StreamUrns,
            Playlist.PlaySuisseStreams,
            Playlist.StreamApples,
            Playlist.StreamGoogles,
            Playlist.BitmovinSamples,
            Playlist.UnifiedStreaming,
            Playlist.UnifiedStreamingDash,
        )
    }
    TvLazyColumn(
        modifier = modifier,
        pivotOffsets = PivotOffsets(0.5f, 0f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "Samples", style = MaterialTheme.typography.displayMedium)
            }
        }
        for (playlist in listItems) {
            item {
                PlaylistHeader(
                    modifier = Modifier.padding(vertical = 6.dp),
                    title = playlist.title
                )
            }
            items(playlist.items) { item ->
                DemoItemView(
                    modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    subtitle = item.description,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Preview
@Composable
private fun ExamplesPreview() {
    MaterialTheme() {
        Surface {
            ExamplesHome()
        }
    }
}
