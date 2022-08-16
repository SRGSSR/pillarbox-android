/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Swi media item source
 *
 * @constructor Create empty Swi media item source
 */
class SwiMediaItemSource : MediaItemSource {
    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        if (mediaItem.mediaId != UNIQUE_SWI_ID) {
            throw IllegalArgumentException("Unkown mediaId = ${mediaItem.mediaId}")
        }
        return mediaItem.buildUpon()
            .setMediaMetadata(
                (
                    mediaItem.mediaMetadata.buildUpon()
                        .setTitle("SWI sample content")
                    ).build()
            )
            .setUri("https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8?start=0.0&end=283.0")
            .build()
    }

    companion object {
        /**
         * Unique SWI ID to use as mediaId of MediaItem
         */
        const val UNIQUE_SWI_ID = "SWI_ID"
    }
}
