/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.net.Uri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter as TimeRangeChapter

internal object ChapterAdapter {
    private val imageScalingService = ImageScalingService()

    fun toChapter(chapter: Chapter): TimeRangeChapter {
        requireNotNull(chapter.fullLengthMarkIn)
        requireNotNull(chapter.fullLengthMarkOut)
        return TimeRangeChapter(
            id = chapter.urn,
            start = chapter.fullLengthMarkIn,
            end = chapter.fullLengthMarkOut,
            mediaMetadata = MediaMetadata.Builder()
                .setTitle(chapter.title)
                .setArtworkUri(Uri.parse(imageScalingService.getScaledImageUrl(chapter.imageUrl)))
                .setDescription(chapter.lead)
                .build()
        )
    }

    fun getChapters(mediaComposition: MediaComposition): List<TimeRangeChapter> {
        val mainChapter = mediaComposition.mainChapter
        if (!mainChapter.isFullLengthChapter && mainChapter.mediaType == MediaType.AUDIO) return emptyList()
        return mediaComposition.listChapter
            .asSequence()
            .filter {
                it != mediaComposition.mainChapter
            }
            .filter {
                it.mediaType == mainChapter.mediaType
            }
            .filter {
                it.fullLengthUrn == mainChapter.urn
            }
            .map {
                toChapter(it)
            }
            .sortedBy { it.start }
            .toList()
    }
}
