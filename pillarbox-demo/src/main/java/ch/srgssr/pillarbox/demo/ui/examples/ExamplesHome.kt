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
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadService
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.service.PillarboxDownloadService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.examples.ExamplesViewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxRenderersFactory
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import java.io.IOException

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

    ListStreamView(playlists = playlists, onDownloadClicked = {
        val mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
            addAssetLoader(SRGAssetLoader(context))
        }
        val mediaItem = it.toMediaItem()
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
        val downloadHelper = DownloadHelper(
            mediaItem,
            mediaSource,
            TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT,
            DefaultRendererCapabilitiesList.Factory(PillarboxRenderersFactory(context)).createRendererCapabilitiesList()
        )
        downloadHelper.prepare(object : DownloadHelper.Callback {
            override fun onPrepared(helper: DownloadHelper) {
                val downloaderRequest = helper.getDownloadRequest(mediaItem.mediaId, null)
                DownloadService.sendAddDownload(context, PillarboxDownloadService::class.java, downloaderRequest, false)
                helper.release()
            }

            override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                TODO("Not yet implemented")
            }
        })
    }) {
        SimplePlayerActivity.startActivity(context, it)
    }
}

@Composable
private fun ListStreamView(
    playlists: List<Playlist>,
    onDownloadClicked: (item: DemoItem) -> Unit,
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
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
                languageTag = playlist.languageTag,
            )

            DemoListSectionView {
                playlist.items.forEachIndexed { index, item ->
                    DemoListItemView(
                        title = item.title ?: "No title",
                        modifier = Modifier.fillMaxWidth(),
                        subtitle = item.description,
                        languageTag = item.languageTag,
                        onClick = { onItemClicked(item) },
                        secondaryClick = { onDownloadClicked(item) }
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
            DemoItem.URL(title = "Title 1", uri = "Uri 1"),
            DemoItem.URL(title = "Title 2", uri = "Uri 2"),
            DemoItem.URL(title = "Title 3", uri = "Uri 3"),
        )
    )
    val playlists = listOf(playlist, playlist.copy(title = "Playlist title 2"))

    PillarboxTheme {
        ListStreamView(playlists = playlists, onDownloadClicked = {}) {
        }
    }
}
