/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import kotlinx.serialization.Serializable

/**
 * Chapter representation exchanged in the Cast `customData`, shared with the SRG SSR web receiver.
 */
@Serializable
internal data class CastChapter(
    val startTime: Long,
    val endTime: Long,
    val identifier: String,
    val posterUrl: String? = null,
    val title: String? = null,
) {
    fun toChapter(): Chapter {
        return Chapter(
            id = identifier,
            start = startTime,
            end = endTime,
            mediaMetadata = MediaMetadata.Builder()
                .setTitle(title)
                .setArtworkUri(posterUrl?.toUri())
                .build()
        )
    }

    companion object {
        fun fromChapter(chapter: Chapter): CastChapter {
            return CastChapter(
                startTime = chapter.start,
                endTime = chapter.end,
                identifier = chapter.id,
                posterUrl = chapter.mediaMetadata.artworkUri?.toString(),
                title = chapter.mediaMetadata.title?.toString(),
            )
        }
    }
}
