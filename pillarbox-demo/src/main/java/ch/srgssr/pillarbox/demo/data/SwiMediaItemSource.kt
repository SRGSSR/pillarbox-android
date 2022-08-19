/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource.Companion.INFINITE_UPDATE_SWI_ID
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource.Companion.SIMPLE_SWI_ID
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource.Companion.TWO_TIMES_UPDATES_SWI_ID
import ch.srgssr.pillarbox.player.data.MediaItemSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

/**
 * A very simple dummy implementation of MediaItemSource that return some predefined MediaItem.
 *  - [SIMPLE_SWI_ID] to get a SWI content without updates.
 *  - [TWO_TIMES_UPDATES_SWI_ID] to get a SWI content with 2 updates.
 *  - [INFINITE_UPDATE_SWI_ID] to get a never ending SWI content update.
 *
 * @constructor Create empty Swi media item source.
 */
class SwiMediaItemSource : MediaItemSource {

    override fun loadMediaItem(mediaItem: MediaItem): Flow<MediaItem> {
        return when (mediaItem.mediaId) {
            SIMPLE_SWI_ID -> {
                flowOf(createMediaItem(mediaItem, "Unique item", SWI_TICINO_URL))
            }
            TWO_TIMES_UPDATES_SWI_ID -> {
                val title = "Title"
                flow {
                    delay(DELAY)
                    emit(createMediaItem(mediaItem, "$title - first", SWI_INSIDE_EARTH_URL, "FirstTag"))
                    delay(DELAY)
                    emit(createMediaItem(mediaItem, "$title - Second", SWI_INSIDE_EARTH_URL, "SecondTag"))
                }
            }
            INFINITE_UPDATE_SWI_ID -> {
                val title = "Infinite title"
                var counter = 0
                flow {
                    while (true) {
                        delay(DELAY)
                        emit(createMediaItem(mediaItem, "$title - $counter", SWI_FONDUE_URL, "FirstTag $counter"))
                        counter++
                    }
                }
            }
            FAILING_REQUEST_SWI_ID -> {
                val title = "Failing at third update"
                flow {
                    delay(DELAY)
                    emit(createMediaItem(mediaItem, "$title - first", SWI_INSIDE_EARTH_URL, "FirstTag"))
                    delay(DELAY)
                    emit(createMediaItem(mediaItem, "$title - Second", SWI_INSIDE_EARTH_URL, "SecondTag"))
                    delay(DELAY)
                    throw IOException("Simulate IOException from server!")
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown mediaId = ${mediaItem.mediaId}")
            }
        }
    }

    companion object {
        /**
         * ID a simple MediaItem
         */
        const val SIMPLE_SWI_ID = "SIMPLE_SWI_ID"

        /**
         * ID to get MediaItem that update two times
         */
        const val TWO_TIMES_UPDATES_SWI_ID = "TWO_TIMES_UPDATES_SWI_ID"

        /**
         * ID to get MediaItem that update indefinitely every [DELAY] ms times.
         */
        const val INFINITE_UPDATE_SWI_ID = "INFINITE_UPDATE_SWI_ID"

        /**
         * ID to get MediaItem taht update twice and then throw a IOException
         */
        const val FAILING_REQUEST_SWI_ID = "FAILING_REQUEST_SWI_ID"
        private const val DELAY = 10000L
        private const val SWI_FONDUE_URL = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8?start=0.0&end=283.0"
        private const val SWI_TICINO_URL = "https://swi-vod.akamaized.net/videoJson/47543164/master.m3u8?start=0.0&end=202.0"
        private const val SWI_INSIDE_EARTH_URL = "https://swi-vod.akamaized.net/videoJson/47749694/master.m3u8?start=0.0&end=318.0"

        private fun createMediaItem(mediaItem: MediaItem, title: String, url: String, tag: String? = null): MediaItem {
            return mediaItem.buildUpon()
                .setUri(url)
                .setTag(tag)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )
                .build()
        }
    }
}
