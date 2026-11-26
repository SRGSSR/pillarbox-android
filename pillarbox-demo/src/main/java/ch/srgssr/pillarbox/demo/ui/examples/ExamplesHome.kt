/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.examples.ExamplesViewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.downloads.DemoItemDownloadButton
import ch.srgssr.pillarbox.demo.ui.downloads.DownloadButton
import ch.srgssr.pillarbox.demo.ui.downloads.DownloadsViewModel
import ch.srgssr.pillarbox.demo.ui.downloads.statusText
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Examples home page.
 *
 * Display all the [DemoItem] in a List, each with a button to download it showing the state of its download.
 */
@Composable
fun ExamplesHome() {
    val examplesViewModel: ExamplesViewModel = viewModel()
    val downloadsViewModel: DownloadsViewModel = viewModel()
    val context = LocalContext.current
    val playlists by examplesViewModel.contents.collectAsState()
    val downloadsById by downloadsViewModel.downloadsById.collectAsStateWithLifecycle()
    val downloadIds = remember(playlists) {
        playlists.flatMap { it.items }.associateWith { downloadsViewModel.getDownloadId(it) }
    }

    ListStreamView(
        playlists = playlists,
        onItemClicked = { SimplePlayerActivity.startActivity(context, it) },
        itemStatus = { item -> downloadIds[item]?.let { downloadsById[it] }?.statusText() },
        downloadButton = { item, title -> DemoItemDownloadButton(item = item, title = title, downloadsViewModel = downloadsViewModel) },
    )
}

/**
 * @param playlists The playlists to display.
 * @param onItemClicked Called when an item is clicked.
 * @param itemStatus Returns the download status of an item, displayed instead of its description, or `null` if it isn't downloaded.
 * @param downloadButton The download button of an item.
 */
@Composable
private fun ListStreamView(
    playlists: List<Playlist>,
    onItemClicked: (item: DemoItem) -> Unit,
    itemStatus: @Composable (item: DemoItem) -> String?,
    downloadButton: @Composable (item: DemoItem, title: String) -> Unit,
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
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
                languageTag = playlist.languageTag,
            )

            DemoListSectionView {
                playlist.items.forEachIndexed { index, item ->
                    val title = item.title ?: "No title"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DemoListItemView(
                            title = title,
                            modifier = Modifier.weight(1f),
                            subtitle = itemStatus(item) ?: item.description,
                            languageTag = item.languageTag,
                            onClick = { onItemClicked(item) },
                        )

                        downloadButton(item, title)
                    }

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
            DemoItem.URL(title = "Title 1", uri = "Uri 1"),
            DemoItem.URL(title = "Title 2", uri = "Uri 2"),
            DemoItem.URL(title = "Title 3", uri = "Uri 3"),
        )
    )
    val playlists = listOf(playlist, playlist.copy(title = "Playlist title 2"))

    PillarboxTheme {
        ListStreamView(
            playlists = playlists,
            onItemClicked = {},
            itemStatus = { null },
            downloadButton = { _, title ->
                DownloadButton(
                    title = title,
                    download = null,
                    isPreparing = false,
                    onDownload = {},
                    onPause = {},
                    onResume = {},
                    onRestart = {},
                )
            },
        )
    }
}
