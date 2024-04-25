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

internal object ChapterAdapter {
    private val imageScalingService = ImageScalingService()

    fun toChapter(chapter: Chapter): ch.srgssr.pillarbox.player.asset.Chapter {
        requireNotNull(chapter.fullLengthMarkIn)
        requireNotNull(chapter.fullLengthMarkOut)
        return ch.srgssr.pillarbox.player.asset.Chapter(
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

    fun getChapters(mediaComposition: MediaComposition): List<ch.srgssr.pillarbox.player.asset.Chapter> {
        val mainChapter = mediaComposition.mainChapter
        if (!mainChapter.isFullLengthChapter && mainChapter.mediaType == MediaType.AUDIO) return emptyList()
        return mediaComposition.listChapter
            .filter {
                it.fullLengthUrn == mainChapter.urn &&
                    it != mediaComposition.mainChapter &&
                    mainChapter.mediaType == it.mediaType
            }
            .map {
                toChapter(it)
            }
            .sortedBy { it.start }
    }
}
