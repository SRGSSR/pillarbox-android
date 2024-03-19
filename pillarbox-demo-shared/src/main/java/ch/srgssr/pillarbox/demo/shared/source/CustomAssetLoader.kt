/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TimelineWithUpdatedMediaItem
import androidx.media3.exoplayer.source.WrappingMediaSource
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader

/**
 * Custom asset loader that always load the same url and the content is not seekable.
 *
 * @param context The context.
 */
class CustomAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration?.uri?.host == "custom-media.ch"
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri("https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"))
        return Asset(
            mediaMetadata = MediaMetadata.Builder()
                .setTitle("${mediaItem.mediaMetadata.title}:NotSeekable")
                .build(),
            mediaSource = NotSeekableMediaSource(mediaSource)
        )
    }
}

/**
 * A [MediaSource] that cannot be seek.
 */
private class NotSeekableMediaSource(mediaSource: MediaSource) :
    WrappingMediaSource(mediaSource) {

    override fun onChildSourceInfoRefreshed(newTimeline: Timeline) {
        super.onChildSourceInfoRefreshed(TimelineWithUpdatedMediaItem(NotSeekableContent(newTimeline), mediaItem))
    }

    /**
     * Let's say the business required that the [NotSeekableMediaSource] cannot be seek at any time.
     * @param timeline The [Timeline] to forward.
     */
    private class NotSeekableContent(timeline: Timeline) : ForwardingTimeline(timeline) {
        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            internalWindow.isSeekable = false
            return internalWindow
        }
    }
}
