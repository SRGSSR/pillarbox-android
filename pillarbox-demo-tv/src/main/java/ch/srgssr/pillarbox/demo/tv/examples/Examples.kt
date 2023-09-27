/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.tv.item.DemoItemView

/**
 * Examples home
 *
 * @param modifier
 * @param onItemSelected
 * @receiver
 */
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
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(listItems) { playlist ->
            PlaylistRow(
                onItemSelected = onItemSelected,
                modifier = Modifier.fillParentMaxHeight(0.1f),
                playlist = playlist
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlaylistRow(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onItemSelected: (DemoItem) -> Unit = {},
) {
    Text(text = playlist.title, style = MaterialTheme.typography.headlineSmall)
    TvLazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(playlist.items) { item ->
            DemoItemView(
                modifier = Modifier.fillParentMaxWidth(0.2f),
                title = item.title,
                subtitle = item.description,
                onClick = { onItemSelected(item) }
            )
        }
    }
}
