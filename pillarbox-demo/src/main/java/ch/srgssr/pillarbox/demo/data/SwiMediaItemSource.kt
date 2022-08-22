/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * A very simple dummy implementation of MediaItemSource
 * Get the [UNIQUE_SWI_ID] MediaItem or throw IllegalArgumentException.
 *
 * @constructor Create empty Swi media item source
 */
class SwiMediaItemSource(demoDataSource: DemoItemDataSource) : MediaItemSource {
    private val demoItemList = demoDataSource.loadDemoItemFromAssets("streams.json")

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        val demoItem = demoItemList.find { demoItem -> demoItem.type == ItemType.MEDIA && demoItem.id == mediaItem.mediaId }
        demoItem?.let {
            return mediaItem.buildUpon()
                .setMediaMetadata(
                    mediaItem.mediaMetadata.buildUpon()
                        .setTitle(it.title)
                        .setDescription(it.description)
                        .build()
                )
                .setUri(it.uri)
                .build()
        } ?: throw IllegalArgumentException("mediaId = ${mediaItem.mediaId} no found")
    }
}
