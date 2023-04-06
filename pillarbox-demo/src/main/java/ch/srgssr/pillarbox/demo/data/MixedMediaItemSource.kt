/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.player.data.MediaItemSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

/**
 * Load MediaItem from [urnMediaItemSource] if the [MediaItem.mediaId] is an urn.
 *
 * In the demo application we are mixing url and urn. To simplify the data, we choose to store
 * urn and url in the [DemoItem.uri] which provide a why to convert it to [MediaItem].
 *
 * @property urnMediaItemSource item source to use with urn
 */
class MixedMediaItemSource(
    private val urnMediaItemSource: MediaCompositionMediaItemSource
) : MediaItemSource {

    override fun loadMediaItem(mediaItem: MediaItem): Flow<MediaItem> {
        return when {
            mediaItem.mediaId.isValidMediaUrn() -> urnMediaItemSource.loadMediaItem(mediaItem)
            mediaItem.mediaId == DemoItem.CONTINUOUS_UPDATE_ID -> continuousDemoItem(mediaItem)
            else -> flowOf(mediaItem)
        }
    }

    private fun continuousDemoItem(mediaItem: MediaItem): Flow<MediaItem> {
        var titleNumber = 0
        return flow {
            while (currentCoroutineContext().isActive) {
                titleNumber++
                val title = "Title with number $titleNumber"
                val item = mediaItem.buildUpon()
                    .setMediaMetadata(mediaItem.mediaMetadata.buildUpon().setTitle(title).build())
                    .setUri(DemoItem.CONTINUOUS_UPDATE_URL)
                    .build()
                emit(item)
                delay(INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val INTERVAL_MS = 15_000L
    }
}
