/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.DemoItemView
import ch.srgssr.pillarbox.demo.ui.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Examples home page
 *
 * Display all the [DemoItem] in a List. Each item is clickable and mapped to [onItemClicked]
 */
@Composable
fun ExamplesHome() {
    val context = LocalContext.current
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
    ListStreamView(playlistList = listItems) {
        SimplePlayerActivity.startActivity(context, it)
    }
}

@Composable
private fun ListStreamView(playlistList: List<Playlist>, onItemClicked: (DemoItem) -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            InsertContentView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                onItemClicked
            )
        }
        for (playlist in playlistList) {
            DemoListHeaderView(title = playlist.title)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (item in playlist.items) {
                    DemoItemView(
                        modifier = Modifier.fillMaxWidth(),
                        title = item.title,
                        subtitle = item.description,
                        onClick = { onItemClicked(item) },
                    )
                }
            }
        }
        Divider()
        Text(text = BuildConfig.VERSION_NAME)
    }
}

@Preview(showBackground = true)
@Composable
private fun ListStreamPreview() {
    val context = LocalContext.current
    val playlist = Playlist(
        "Playlist title",
        listOf(
            DemoItem(title = "Title 1", uri = "Uri 1"),
            DemoItem(title = "Title 2", uri = "Uri 2"),
            DemoItem(title = "Title 3", uri = "Uri 3"),
        )
    )
    val listPlaylist = listOf(playlist, playlist.copy(title = "play list 2"))
    PillarboxTheme {
        ListStreamView(playlistList = listPlaylist) {
            Toast.makeText(context, "${it.title} clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
