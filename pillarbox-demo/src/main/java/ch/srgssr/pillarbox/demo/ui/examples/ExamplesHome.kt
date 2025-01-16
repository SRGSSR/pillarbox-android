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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
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

    ListStreamView(playlists = playlists, onDownloadClicked = {
        Log.d("DOWNLOAD", "Start downloading...")
        val assetLoader = SRGAssetLoader(context)
        val defaultMediaSource = DefaultMediaSourceFactory(DefaultDataSource.Factory(context, OkHttpDataSource.Factory(PillarboxOkHttp())))
        val mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
            defaultAssetLoader = UrlAssetLoader(defaultMediaSource)
            addAssetLoader(assetLoader)
        }
        val mediaItem = it.toMediaItem()
        MainScope().launch(Dispatchers.IO) {
            Log.d("DOWNLOAD", "Asset item = ${mediaItem.localConfiguration?.uri}")
            val asset = assetLoader.loadAsset(mediaItem = mediaItem)
            val mediaSource = asset.mediaSource
            val mediaItemForDownload = mediaSource.mediaItem.buildUpon()
                .setMediaId(mediaItem.mediaId)
                .setMediaMetadata(asset.mediaMetadata)
                .build()
            Log.d("DOWNLOAD", "${mediaItemForDownload.localConfiguration?.uri}")
            val downloadHelper = DownloadHelper(
                mediaItemForDownload,
                mediaSource,
                DownloadHelper.getDefaultTrackSelectorParameters(context),
                DefaultRendererCapabilitiesList.Factory(PillarboxRenderersFactory(context)).createRendererCapabilitiesList()
            )
            downloadHelper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(helper: DownloadHelper) {
                    Log.d("DOWNLOAD", "onPrepared")
                    val downloaderRequest = helper.getDownloadRequest(mediaItem.mediaId, null)
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
