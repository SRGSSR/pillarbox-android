/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import kotlinx.serialization.Serializable

@Serializable
internal data class CastChapter(
    val startTime: Long,
    val endTime: Long,
    val identifier: String,
    val posterUrl: String,
    val title: String,
) {
    fun toChapter(): Chapter {
        return Chapter(
            id = identifier,
            start = startTime,
            end = endTime,
            mediaMetadata = androidx.media3.common.MediaMetadata.Builder().setTitle("TEST $title").setArtworkUri(posterUrl.toUri()).build()
        )
    }
}
