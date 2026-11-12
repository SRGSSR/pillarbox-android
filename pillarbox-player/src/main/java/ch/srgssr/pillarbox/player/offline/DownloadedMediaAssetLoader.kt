/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

/**
 * [AssetLoader] playing the media provided by a [DownloadedMediaProvider] from its cache.
 *
 * @param downloadedMediaProvider The [DownloadedMediaProvider] providing the downloaded media.
 * @param upstreamDataSourceFactory The [DataSource.Factory] used if some data is missing from the cache.
 * @param findAssetLoader Returns the [AssetLoader] able to load a [MediaItem] when it is not downloaded, or `null` if none. It decodes the
 * trackers data stored with the download.
 */
internal class DownloadedMediaAssetLoader(
    private val downloadedMediaProvider: DownloadedMediaProvider,
    upstreamDataSourceFactory: DataSource.Factory,
    private val findAssetLoader: (MediaItem) -> AssetLoader? = { null },
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
            trackersData = getTrackersData(mediaItem, downloadRequest),
            mediaMetadata = mediaItem.mediaMetadata,
        )
    }

    private fun getTrackersData(mediaItem: MediaItem, downloadRequest: DownloadRequest): MediaItemTrackerData {
        return downloadedMediaProvider.getOfflineTrackersData(downloadRequest)
            ?.let { data -> findAssetLoader(mediaItem)?.decodeOfflineTrackersData(data) }
            ?: MutableMediaItemTrackerData.EMPTY.toMediaItemTrackerData()
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
