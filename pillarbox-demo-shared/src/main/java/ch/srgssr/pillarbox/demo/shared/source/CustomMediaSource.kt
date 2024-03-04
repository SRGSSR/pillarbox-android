/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SuspendMediaSource
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * A [MediaSource] thant play only the Swiss info fondue stream and can't be seek.
 *
 * @param mediaItem The [MediaItem] to load.
 * @param mediaSourceFactory The [MediaSource.Factory] that create the [MediaSource].
 */
class CustomMediaSource private constructor(mediaItem: MediaItem, private val mediaSourceFactory: MediaSource.Factory) :
    SuspendMediaSource(mediaItem) {

    private data class Asset(val uri: Uri, val mediaMetadata: MediaMetadata)

    /**
     * Load meta data from internet
     *
     * @param mediaItem The initial [MediaItem].
     * @return [MediaItem] with the uri to play and override MediaItem.mediaMetadata.title.
     */
    private suspend fun loadMetaDataFromInternet(mediaItem: MediaItem): Asset {
        delay(1.seconds) // Simulate network call.
        return Asset(
            uri = Uri.parse("https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"),
            mediaMetadata = MediaMetadata.Builder()
                .setTitle("${mediaItem.mediaMetadata.title}:NotSeekable")
                .build()
        )
    }

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        val assetToLoad = loadMetaDataFromInternet(mediaItem)
        val mediaItemLoaded = MediaItem.Builder()
            .setUri(assetToLoad.uri)
            .setMediaMetadata(assetToLoad.mediaMetadata)
            .build()
        updateMediaItem(mediaItemLoaded)
        return mediaSourceFactory.createMediaSource(mediaItemLoaded)
    }

    override fun onChildSourceInfoRefreshed(childSourceId: String?, mediaSource: MediaSource, newTimeline: Timeline) {
        super.onChildSourceInfoRefreshed(childSourceId, mediaSource, NoneSeekableContent(newTimeline))
    }

    /**
     * Let's say the business required that the [CustomMediaSource] cannot be seek at any time.
     * @param timeline The [Timeline] to forward.
     */
    private class NoneSeekableContent(timeline: Timeline) : ForwardingTimeline(timeline) {
        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            internalWindow.isSeekable = false
            return internalWindow
        }
    }

    /**
     * A Factory that create [CustomMediaSource] for demo purpose that use [DefaultMediaSourceFactory].
     *
     * @param context The context.
     */
    class Factory(context: Context) : PillarboxMediaSourceFactory.DelegateFactory(DefaultMediaSourceFactory(context)) {

        override fun handleMediaItem(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.uri?.host == "custom-media.ch"
        }

        override fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: MediaSource.Factory): MediaSource {
            return CustomMediaSource(mediaItem, mediaSourceFactory)
        }
    }
}
