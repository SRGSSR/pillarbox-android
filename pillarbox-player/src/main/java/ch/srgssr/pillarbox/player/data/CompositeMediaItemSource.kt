/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.data

import androidx.media3.common.MediaItem

class CompositeMediaItemSource : MediaItemSource {
    val list: List<MediaItemSource> = emptyList()

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        for (source in list) {
            if (source.handle(mediaItem)) return source.loadMediaItem(mediaItem)
        }
        return mediaItem
    }

    override fun handle(mediaItem: MediaItem): Boolean {
        for (source in list) {
            if (source.handle(mediaItem)) return true
        }
        return false
    }
}
