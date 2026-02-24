/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.util.Log
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
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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

    ListStreamView(playlists = playlists, onDownloadClicked = { demoItem ->
        Log.d("DOWNLOAD", "Start downloading...")
        val defaultMediaSource = DefaultMediaSourceFactory(DefaultDataSource.Factory(context, OkHttpDataSource.Factory(PillarboxOkHttp())))
        val mediaItem = demoItem.toMediaItem()
        MainScope().launch(Dispatchers.IO) {
            Log.d("DOWNLOAD", "Asset item = ${mediaItem.localConfiguration?.uri}")
            val mediaSource = defaultMediaSource.createMediaSource(mediaItem)
            val mediaItemForDownload = mediaSource.mediaItem.buildUpon()
                .setMediaId(mediaItem.mediaId)
                .build()
            Log.d("DOWNLOAD", "${mediaItemForDownload.localConfiguration?.uri}")
            val downloadHelper = DownloadHelper(
                mediaItemForDownload,
                mediaSource,
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS,
                DefaultRendererCapabilitiesList.Factory(PillarboxRenderersFactory(context)).createRendererCapabilitiesList()
            )
            downloadHelper.prepare(object : DownloadHelper.Callback {

                override fun onPrepared(helper: DownloadHelper, tracksInfoAvailable: Boolean) {
                    Log.d("DOWNLOAD", "onPrepared $mediaItem")
                    val downloadId = if (mediaItem.mediaId == MediaItem.DEFAULT_MEDIA_ID) mediaItem.localConfiguration?.uri.toString() else mediaItem.mediaId
                    val downloaderRequest = helper.getDownloadRequest(checkNotNull(downloadId), null)
                    DownloadService.sendAddDownload(context, PillarboxDownloadService::class.java, downloaderRequest, false)
                    helper.release()
                }

                override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                    Log.e("DOWNLOAD", "onPrepareError", e)
                    helper.release()
                }
            })
        }
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
                        secondaryClick = if (item.downloadable) {
                            { onDownloadClicked(item) }
                        } else {
                            null
                        }
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
