/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srg.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.RemoteResult
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Load [MediaItem] playable from a [ch.srg.pillarbox.core.business.integrationlayer.data.MediaComposition]
 *
 * Load a [MediaItem] from it's urn set in [MediaItem.mediaId] property.
 * Fill [MediaItem.mediaMetadata] from [MediaItem] if the field is not already set :
 * - [MediaMetadata.title] with [Chapter.title]
 * - [MediaMetadata.subtitle] with [Chapter.lead]
 * - [MediaMetadata.description] with [Chapter.description]
 *
 * @property mediaCompositionDataSource
 */
class MediaCompositionMediaItemSource(private val mediaCompositionDataSource: MediaCompositionDataSource) : MediaItemSource {

    private fun fillMetaData(metadata: MediaMetadata, chapter: Chapter): MediaMetadata {
        val builder = metadata.buildUpon()
        metadata.title ?: builder.setTitle(chapter.title)
        metadata.subtitle ?: builder.setSubtitle(chapter.lead)
        metadata.description ?: builder.setDescription(chapter.description)
        return builder.build()
    }

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        if (mediaItem.mediaId == MediaItem.DEFAULT_MEDIA_ID) {
            throw IllegalArgumentException("Set a mediaId")
        }
        when (val result = mediaCompositionDataSource.getMediaCompositionByUrn(mediaItem.mediaId)) {
            is RemoteResult.Success -> {
                Log.d("Pillarbox", "${result.data} ${result.data.mainChapter}")
                val chapter = result.data.mainChapter
                return mediaItem.buildUpon()
                    .setMediaMetadata(fillMetaData(mediaItem.mediaMetadata, chapter))
                    .setUri(chapter.listResource?.first()?.url)
                    .build()
            }
            is RemoteResult.Error -> {
                throw result.throwable
            }
        }
    }
}
