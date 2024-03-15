package ch.srgssr.pillarbox.player.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader

/**
 * Pillarbox media source factory create a new [PillarboxMediaSource] from a MediaItem.
 * It select the first [AssetLoader] to use by checking if [AssetLoader.canLoadAsset].
 *
 * @param context to create the [defaultAssetLoader].
 */
class PillarboxMediaSourceFactory(context: Context) : MediaSource.Factory {
    /**
     * Default asset loader used when no other AssetLoader has been found.
     */
    val defaultAssetLoader = UrlAssetLoader(DefaultMediaSourceFactory(context))

    /**
     * Minimal duration in milliseconds to consider a live with seek capabilities.
     */
    var minLiveDvrDurationMs = LIVE_DVR_MIN_DURATION_MS
    private val listAssetLoader = ArrayList<AssetLoader>()

    /**
     * Add asset loader
     *
     * @param index index at which the specified element is to be inserted element – element to be inserted
     * @param assetLoader [AssetLoader] to insert.
     */
    fun addAssetLoader(index: Int, assetLoader: AssetLoader) {
        check(assetLoader !is UrlAssetLoader) { "Already in the factory by default" }
        listAssetLoader.add(index, assetLoader)
    }

    /**
     * Add asset loader
     *
     * @param assetLoader [AssetLoader] to insert.
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
        return PillarboxMediaSource(mediaItem = mediaItem, assetLoader = assetLoader, minLiveDvrDurationMs = minLiveDvrDurationMs)
    }

    companion object {
        private const val LIVE_DVR_MIN_DURATION_MS = 60_000L
    }
}
