package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Pillarbox media source factory create a new PillarboxMediaSource from a MediaItem
 *
 * @param defaultMediaSourceFactory default MediaSourceFactory to load real underlying MediaSource
 * @param mediaItemSource for loading asynchronously a MediaItem
 * @constructor Create empty Pillarbox media source factory
 */
class PillarboxMediaSourceFactory(
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val mediaItemSource: MediaItemSource
) : MediaSource.Factory {
    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): MediaSource.Factory {
        return defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        return defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
    }

    override fun getSupportedTypes(): IntArray {
        return defaultMediaSourceFactory.supportedTypes
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        return PillarboxMediaSource(mediaItem, mediaItemSource, defaultMediaSourceFactory)
    }
}
