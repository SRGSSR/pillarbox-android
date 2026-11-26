/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader

/**
 * [AssetLoader] playing the media provided by a [DownloadedMediaProvider] from its cache.
 *
 * @param downloadedMediaProvider The [DownloadedMediaProvider] providing the downloaded media.
 * @param upstreamDataSourceFactory The [DataSource.Factory] used if some data is missing from the cache.
 */
internal class DownloadedMediaAssetLoader(
    private val downloadedMediaProvider: DownloadedMediaProvider,
    upstreamDataSourceFactory: DataSource.Factory,
) : AssetLoader(
    mediaSourceFactory = DefaultMediaSourceFactory(createReadOnlyCacheDataSourceFactory(downloadedMediaProvider, upstreamDataSourceFactory))
) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return downloadedMediaProvider.getDownloadRequest(mediaItem) != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val downloadRequest = checkNotNull(downloadedMediaProvider.getDownloadRequest(mediaItem)) {
            "${mediaItem.mediaId} is not downloaded"
        }
        // The MediaItem created from the request contains the stream keys of the downloaded tracks and the custom cache key.
        val downloadedMediaItem = downloadRequest.toMediaItem()

        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(downloadedMediaItem),
            mediaMetadata = mediaItem.mediaMetadata,
        )
    }
}

/**
 * Creates a [DataSource.Factory] reading from the [DownloadedMediaProvider.cache] first, and from [upstreamDataSourceFactory] otherwise.
 * Nothing is written to the cache, so that streaming doesn't fill the download cache.
 */
internal fun createReadOnlyCacheDataSourceFactory(
    downloadedMediaProvider: DownloadedMediaProvider,
    upstreamDataSourceFactory: DataSource.Factory,
): DataSource.Factory {
    return CacheDataSource.Factory()
        .setCache(downloadedMediaProvider.cache)
        .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
        .setCacheWriteDataSinkFactory(null)
}
