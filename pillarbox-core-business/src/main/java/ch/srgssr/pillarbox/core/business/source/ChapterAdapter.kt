/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.net.Uri
import androidx.media3.common.MediaMetadata
import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.MediaComposition
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter as TimeRangeChapter

internal object ChapterAdapter {
    private val imageScalingService = ImageScalingService()

    fun toChapter(chapter: Chapter): TimeRangeChapter {
        val fullLengthMarkIn = chapter.fullLengthMarkIn
        val fullLengthMarkOut = chapter.fullLengthMarkOut
        requireNotNull(fullLengthMarkIn)
        requireNotNull(fullLengthMarkOut)
        return TimeRangeChapter(
            id = chapter.urn,
            start = fullLengthMarkIn,
            end = fullLengthMarkOut,
            mediaMetadata = MediaMetadata.Builder()
                .setTitle(chapter.title)
                .setArtworkUri(Uri.parse(imageScalingService.getScaledImageUrl(chapter.imageUrl.rawUrl)))
                .setDescription(chapter.lead)
                .build()
        )
    }

    fun getChapters(mediaComposition: MediaComposition): List<TimeRangeChapter> {
        val mainChapter = mediaComposition.getMainChapter()
        if (mainChapter.mediaType == MediaType.AUDIO) return emptyList()
        return mediaComposition.chapterList
            .asSequence()
            .filter {
                it != mainChapter
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
