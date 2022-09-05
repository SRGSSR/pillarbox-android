/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srg.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.RemoteResult
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Urn media item source
 *
 * @property mediaCompositionDataSource
 * @constructor Create empty Urn media item source
 */
class UrnMediaItemSource(private val mediaCompositionDataSource: MediaCompositionDataSource) : MediaItemSource {

    private fun fillMetaData(metadata: MediaMetadata, chapter: Chapter): MediaMetadata {
        val builder = metadata.buildUpon()
        metadata.title ?: builder.setTitle(chapter.title)
        metadata.subtitle ?: builder.setSubtitle(chapter.lead)
        metadata.description ?: builder.setDescription(chapter.description)
        return builder.build()
    }

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        when (val result = mediaCompositionDataSource.getMediaCompositionByUrn(mediaItem.mediaId)) {
            is RemoteResult.Success -> {
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
