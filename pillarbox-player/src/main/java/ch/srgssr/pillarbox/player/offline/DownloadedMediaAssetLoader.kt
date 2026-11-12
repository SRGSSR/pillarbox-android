/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
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
    init {
        setDrmSessionManagerProvider(DefaultDrmSessionManagerProvider())
    }

    /**
     * Sets the [DrmSessionManagerProvider] used for the downloaded media that are not played with their offline licenses.
     *
     * @param drmSessionManagerProvider The [DrmSessionManagerProvider].
     */
    fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider) {
        mediaSourceFactory.setDrmSessionManagerProvider(OfflineDrmSessionManagerProvider(drmSessionManagerProvider))
    }

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return downloadedMediaProvider.getDownloadRequest(mediaItem) != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val downloadRequest = checkNotNull(downloadedMediaProvider.getDownloadRequest(mediaItem)) {
            "${mediaItem.mediaId} is not downloaded"
        }
        // The MediaItem created from the request contains the stream keys of the downloaded tracks and the custom cache key.
        val offlineLicenses = downloadedMediaProvider.getOfflineLicenses(downloadRequest)
        val downloadedMediaItem = downloadRequest.toMediaItem(createMediaItemBuilder(offlineLicenses))

        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(downloadedMediaItem),
            mediaMetadata = mediaItem.mediaMetadata,
        )
    }
}

/**
 * Creates the [MediaItem.Builder] used to play a downloaded media. If it has [offlineLicenses], they are set as the
 * [tag][MediaItem.LocalConfiguration.tag] of the [MediaItem], so that an [OfflineDrmSessionManagerProvider] plays the media with them.
 */
internal fun createMediaItemBuilder(offlineLicenses: List<OfflineLicense>): MediaItem.Builder {
    val builder = MediaItem.Builder()
    if (offlineLicenses.isNotEmpty()) {
        builder.setDrmConfiguration(
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setKeySetId(offlineLicenses.first().keySetId)
                .setMultiSession(true)
                .build()
        )
        builder.setTag(OfflineLicenses(offlineLicenses))
    }
    return builder
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
