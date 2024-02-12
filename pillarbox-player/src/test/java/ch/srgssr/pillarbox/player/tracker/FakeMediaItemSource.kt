/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData

class FakeMediaItemSource : MediaItemSource {
    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        val trackerData = mediaItem.getMediaItemTrackerData()
        val itemBuilder = if (mediaItem.localConfiguration == null) {
            val url = when (mediaItem.mediaId) {
                MEDIA_ID_1 -> URL_MEDIA_1
                MEDIA_ID_2 -> URL_MEDIA_2
                else -> URL_MEDIA_3
            }
            mediaItem.buildUpon().setUri(url)
        } else {
            mediaItem.buildUpon()
        }

        if (mediaItem.mediaId == MEDIA_ID_NO_TRACKING_DATA) return itemBuilder.build()
        itemBuilder.setTrackerData(
            trackerData.buildUpon().putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(mediaItem.mediaId)).build()
        )
        return itemBuilder.build()
    }

    companion object {
        const val MEDIA_ID_1 = "media:1"
        const val MEDIA_ID_2 = "media:2"
        const val MEDIA_ID_NO_TRACKING_DATA = "media:3"

        const val URL_MEDIA_1 = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
        const val URL_MEDIA_2 = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
        const val URL_MEDIA_3 = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"

        const val NEAR_END_POSITION_MS = 15_000L // the video has 17 sec duration
    }
}
