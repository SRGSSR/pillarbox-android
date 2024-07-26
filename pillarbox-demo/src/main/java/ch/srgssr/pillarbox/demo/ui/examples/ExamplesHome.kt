/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.examples.ExamplesViewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Examples home page.
 *
 * Display all the [DemoItem] in a List.
 */
@Composable
fun ExamplesHome() {
    val examplesViewModel: ExamplesViewModel = viewModel()
    val context = LocalContext.current
    val playlists by examplesViewModel.contents.collectAsState()

    ListStreamView(playlists = playlists) {
        SimplePlayerActivity.startActivity(context, it)
    }
}

@Composable
private fun ListStreamView(
    playlists: List<Playlist>,
    onItemClicked: (item: DemoItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.paddings.baseline),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
    ) {
        item(contentType = "url_urn_input") {
            Card(modifier = Modifier.fillMaxWidth()) {
                InsertContentView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.paddings.small),
                    onPlayClick = onItemClicked
                )
            }
        }

        items(
            items = playlists,
            contentType = { "playlist" }
        ) { playlist ->
            DemoListHeaderView(
                title = playlist.title,
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
            )

            DemoListSectionView {
                playlist.items.forEachIndexed { index, item ->
                    DemoListItemView(
                        title = item.title,
                        modifier = Modifier.fillMaxWidth(),
                        subtitle = item.description,
                        onClick = { onItemClicked(item) },
                    )

                    if (index < playlist.items.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ListStreamPreview() {
    val playlist = Playlist(
        "Playlist title 1",
        listOf(
            DemoItem(title = "Title 1", uri = "Uri 1"),
            DemoItem(title = "Title 2", uri = "Uri 2"),
            DemoItem(title = "Title 3", uri = "Uri 3"),
        )
    )
    val playlists = listOf(playlist, playlist.copy(title = "Playlist title 2"))

    PillarboxTheme {
        ListStreamView(playlists = playlists) {
        }
    }
}
