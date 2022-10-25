/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Playlists home view that display list of clickable playlist
 *
 * @param playlistsViewModel
 */
@Composable
fun PlaylistsHome(playlistsViewModel: PlaylistsViewModel) {
    val context = LocalContext.current
    val listItems = playlistsViewModel.listPlaylist.collectAsState()
    Column {
        for (playlist in listItems.value) {
            PlaylistItemView(playlist = playlist) {
                SimplePlayerActivity.startActivity(context, playlist)
            }
            Divider()
        }
    }
}

@Composable
private fun PlaylistItemView(playlist: Playlist, onItemClicked: (Playlist) -> Unit) {
    Text(
        modifier = Modifier
            .clickable { onItemClicked(playlist) }
            .padding(8.dp),
        text = playlist.title,
        style = MaterialTheme.typography.body1
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPlaylistItem() {
    PillarboxTheme {
        PlaylistItemView(playlist = Playlist("Playlist Title", emptyList())) {
            // Nothing
        }
    }
}
