/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UniqueMediaItemSource(private val url: String) : MediaItemSource {

    override fun loadMediaItem(mediaItem: MediaItem): Flow<MediaItem> {
        return flowOf(mediaItem.buildUpon().setUri(url).build())
    }

    companion object {
        fun createMediaItem(mediaId: String = "dummyId"): MediaItem {
            return MediaItem.Builder().setMediaId(mediaId).build()
        }
    }
}
