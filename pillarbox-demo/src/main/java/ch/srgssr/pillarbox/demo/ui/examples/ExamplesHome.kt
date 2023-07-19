/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
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
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            InsertContentView(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                onItemClicked
            )
        }
        for (playlist in playlistList) {
            PlaylistHeaderView(playlist = playlist)
            Column {
                for (item in playlist.items) {
                    DemoItemView(item = item, onItemClicked = onItemClicked)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeaderView(playlist: Playlist) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            text = playlist.title,
            style = MaterialTheme.typography.h4
        )
        Divider()
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

@Composable
private fun DemoItemView(item: DemoItem, onItemClicked: (DemoItem) -> Unit) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onItemClicked(item) }
            .wrapContentHeight()
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
    ) {
        Text(text = item.title, style = MaterialTheme.typography.body2)
        item.description?.let {
            Text(text = item.description, style = MaterialTheme.typography.caption)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoItemPreview() {
    val context = LocalContext.current
    val demoItem = DemoItem(title = "The title of the media", description = "Description of the media", uri = "id")
    PillarboxTheme {
        DemoItemView(item = demoItem) {
            Toast.makeText(context, "${it.title} clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
