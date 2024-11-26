/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import kotlin.time.TimeSource

/**
 * A factory for creating [PillarboxMediaSource] instances.
 *
 * This factory selects the first suitable [AssetLoader] to use for a given [MediaItem] by checking if [AssetLoader.canLoadAsset] returns `true`.
 *
 * @param context The [Context] used to create the default [AssetLoader].
 * @param timeSource The [TimeSource] to use for the created [MediaSource].
 */
class PillarboxMediaSourceFactory(
    context: Context,
    private val timeSource: TimeSource = TimeSource.Monotonic
) : MediaSource.Factory {
    /**
     * The default [AssetLoader] used to load assets when no other [AssetLoader] is able to handle the request.
     */
    val defaultAssetLoader = UrlAssetLoader(
        DefaultMediaSourceFactory(
            DefaultDataSource.Factory(
                context,
                OkHttpDataSource.Factory(PillarboxOkHttp())
            )
        )
    )

    /**
     * The minimum duration of the live stream, in milliseconds, for it to be considered a live stream with DVR capabilities.
     */
    var minLiveDvrDurationMs = LIVE_DVR_MIN_DURATION_MS
    private val listAssetLoader = mutableListOf<AssetLoader>()

    /**
     * Adds an [AssetLoader] at the specified index.
     *
     * @param index The index at which the [AssetLoader] should be added.
     * @param assetLoader The [AssetLoader] to add.
     */
    fun addAssetLoader(index: Int, assetLoader: AssetLoader) {
        check(assetLoader !is UrlAssetLoader) { "Already in the factory by default" }
        listAssetLoader.add(index, assetLoader)
    }

    /**
     * Adds an [AssetLoader].
     *
     * @param assetLoader The [AssetLoader] to add.
     */
    fun addAssetLoader(assetLoader: AssetLoader) {
        check(assetLoader !is UrlAssetLoader) { "Already in the factory by default" }
        listAssetLoader.add(assetLoader)
    }

    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        for (assetLoader in listAssetLoader) {
            assetLoader.mediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        }
        defaultAssetLoader.mediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        for (assetLoader in listAssetLoader) {
            assetLoader.mediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        }
        defaultAssetLoader.mediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    override fun getSupportedTypes(): IntArray {
        return defaultAssetLoader.mediaSourceFactory.supportedTypes
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        checkNotNull(mediaItem.localConfiguration)
        val assetLoader = listAssetLoader.firstOrNull { it.canLoadAsset(mediaItem) } ?: defaultAssetLoader
        return PillarboxMediaSource(
            mediaItem = mediaItem,
            assetLoader = assetLoader,
            minLiveDvrDurationMs = minLiveDvrDurationMs,
            timeSource = timeSource
        )
    }

    private companion object {
        private const val LIVE_DVR_MIN_DURATION_MS = 60_000L
    }
}
