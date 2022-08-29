/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A very simple [MediaItemSource] that provide [MediaItem] from [DemoItemDataSource]
 */
class DemoMediaItemSource(demoDataSource: DemoItemDataSource) : MediaItemSource {
    private val demoItemList = demoDataSource.loadDemoItemFromAssets("streams.json")

    override fun loadMediaItem(mediaItem: MediaItem): Flow<MediaItem> {
        val demoItem = demoItemList.find { demoItem -> demoItem.type == ItemType.MEDIA && demoItem.id == mediaItem.mediaId }
        demoItem?.let {
            return flowOf(
                mediaItem.buildUpon()
                    .setMediaMetadata(
                        mediaItem.mediaMetadata.buildUpon()
                            .setTitle(it.title)
                            .setDescription(it.description)
                            .build()
                    )
                    .setUri(it.uri)
                    .build()
            )
        } ?: throw IllegalArgumentException("mediaId = ${mediaItem.mediaId} no found")
    }
}
