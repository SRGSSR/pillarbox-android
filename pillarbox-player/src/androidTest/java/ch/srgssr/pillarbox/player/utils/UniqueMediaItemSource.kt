/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource

class UniqueMediaItemSource(private val url: String) : MediaItemSource {

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        return mediaItem.buildUpon().setUri(url).build()
    }

    companion object {
        fun createMediaItem(mediaId: String = "dummyId"): MediaItem {
            return MediaItem.Builder().setMediaId(mediaId).build()
        }
    }
}
