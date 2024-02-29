package ch.srgssr.pillarbox.player.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSource.Factory
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * Pillarbox media source factory create a new PillarboxMediaSource from a MediaItem
 *
 * @param defaultMediaSourceFactory default MediaSourceFactory to load real underlying MediaSource
 */
class PillarboxMediaSourceFactory(
    private var defaultMediaSourceFactory: DefaultMediaSourceFactory,
) : Factory {
    private val listDelegateFactory = ArrayList<DelegateFactory>()

    /**
     * Create with a [DefaultMediaSourceFactory].
     */
    constructor(context: Context) : this(DefaultMediaSourceFactory(context))

    /**
     * Add media source factory
     *
     * @param factory The [DelegateFactory] to add.
     */
    fun addMediaSourceFactory(factory: DelegateFactory) {
        listDelegateFactory.add(factory)
    }

    /**
     * Add media source factory
     *
     * @param index index at which the specified element is to be inserted.
     * @param factory The [DelegateFactory] to insert.
     */
    fun addMediaSourceFactory(index: Int, factory: DelegateFactory) {
        listDelegateFactory.add(index, factory)
    }

    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): Factory {
        return defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        return defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
    }

    override fun getSupportedTypes(): IntArray {
        return defaultMediaSourceFactory.supportedTypes
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        return listDelegateFactory.firstOrNull {
            it.handleMediaItem(mediaItem)
        }?.createMediaSource(mediaItem) ?: defaultMediaSourceFactory
            .createMediaSource(mediaItem)
    }

    /**
     * Delegate factory
     *
     * @param mediaSourceFactory called when handleMediaItem return false
     */
    abstract class DelegateFactory(private val mediaSourceFactory: Factory) : Factory by mediaSourceFactory {

        /**
         * Handle media item
         *
         * @param mediaItem The [MediaItem].
         * @return true if this factory can create a [MediaSource] from [mediaItem].
         */
        abstract fun handleMediaItem(mediaItem: MediaItem): Boolean

        protected abstract fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: Factory): MediaSource

        final override fun createMediaSource(mediaItem: MediaItem): MediaSource {
            if (handleMediaItem(mediaItem)) {
                return createMediaSourceInternal(mediaItem, mediaSourceFactory)
            }
            return mediaSourceFactory.createMediaSource(mediaItem)
        }
    }
}
