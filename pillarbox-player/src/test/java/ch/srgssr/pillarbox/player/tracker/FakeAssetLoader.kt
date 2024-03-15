/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader

class FakeAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val itemBuilder = mediaItem.buildUpon()
        val trackerData = if (mediaItem.mediaId == MEDIA_ID_NO_TRACKING_DATA) MediaItemTrackerData.EMPTY
        else MediaItemTrackerData.Builder()
            .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(mediaItem.mediaId))
            .build()
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(itemBuilder.build()),
            trackersData = trackerData,
            mediaMetadata = mediaItem.mediaMetadata
        )
    }

    companion object {
        const val MEDIA_ID_1 = "media:1"
        const val MEDIA_ID_2 = "media:2"
        const val MEDIA_ID_NO_TRACKING_DATA = "media:3"

        val MEDIA_1 = createMediaItem(MEDIA_ID_1)
        val MEDIA_2 = createMediaItem(MEDIA_ID_2)
        val MEDIA_NO_TRACKING_DATA = createMediaItem(MEDIA_ID_NO_TRACKING_DATA)

        private const val URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"

        const val NEAR_END_POSITION_MS = 15_000L // the video has 17 sec duration

        private fun createMediaItem(mediaId: String) = MediaItem.Builder()
            .setUri(URL)
            .setMediaId(mediaId)
            .build()
    }
}
