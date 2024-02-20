package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
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
) : MediaSource.Factory by defaultMediaSourceFactory {

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        return PillarboxMediaSource(mediaItem, mediaItemSource, defaultMediaSourceFactory)
    }
}
