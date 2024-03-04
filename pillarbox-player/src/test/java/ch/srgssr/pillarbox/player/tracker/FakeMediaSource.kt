/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SuspendMediaSource

fun interface MediaItemLoader {
    suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem
}

class FakeMediaSource(mediaItem: MediaItem, private val mediaSourceFactory: MediaSource.Factory, private val mediaItemSource: MediaItemLoader) :
    SuspendMediaSource
        (mediaItem) {

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        val loadedMediaItem = mediaItemSource.loadMediaItem(mediaItem)
        updateMediaItem(loadedMediaItem)
        return mediaSourceFactory.createMediaSource(loadedMediaItem)
    }

    class Factory(
        context: Context,
        private val mediaItemSource: MediaItemLoader = FakeMediaItemSource()
    ) :
        PillarboxMediaSourceFactory.DelegateFactory
            (
                DefaultMediaSourceFactory
                (context)
            ) {
        override fun handleMediaItem(mediaItem: MediaItem): Boolean {
            return true
        }

        override fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: MediaSource.Factory): MediaSource {
            return FakeMediaSource(mediaItem, mediaSourceFactory, mediaItemSource)
        }
    }

    class FakeMediaItemSource : MediaItemLoader {
        override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
            val trackerData = mediaItem.getMediaItemTrackerData()
            val itemBuilder = mediaItem.buildUpon()

            if (mediaItem.localConfiguration == null) {
                val url = when (mediaItem.mediaId) {
                    MEDIA_ID_1 -> URL_MEDIA_1
                    MEDIA_ID_2 -> URL_MEDIA_2
                    else -> URL_MEDIA_3
                }
                itemBuilder.setUri(url)
            }

            if (mediaItem.mediaId == MEDIA_ID_NO_TRACKING_DATA) return itemBuilder.build()
            itemBuilder.setTrackerData(
                trackerData.buildUpon().putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(mediaItem.mediaId)).build()
            )
            return itemBuilder.build()
        }
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
