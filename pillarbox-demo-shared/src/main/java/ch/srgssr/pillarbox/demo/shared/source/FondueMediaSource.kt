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
 * A [MediaSource] thant play only the fondue stream.
 *
 * @param mediaItem
 * @param mediaSourceFactory
 */
class FondueMediaSource private constructor(mediaItem: MediaItem, private val mediaSourceFactory: MediaSource.Factory) :
    SuspendMediaSource(mediaItem) {

    private suspend fun loadMetaDataFromInternet(id: String): Uri {
        delay(1.seconds)
        return Uri.parse("https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8")
    }

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        val assetToLoad = loadMetaDataFromInternet(mediaItem.mediaId)
        // Don't forget to updateMediaItem if metadata has been changed
        updateMediaItem(
            mediaItem.buildUpon()
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("${mediaItem.mediaMetadata.title}:NotSeekable")
                        .build()
                )
                .build()
        )

        return mediaSourceFactory.createMediaSource(MediaItem.fromUri(assetToLoad))
    }

    override fun onChildSourceInfoRefreshed(childSourceId: String?, mediaSource: MediaSource, newTimeline: Timeline) {
        super.onChildSourceInfoRefreshed(childSourceId, mediaSource, NoneSeekableContent(newTimeline))
    }

    /**
     * Let's say the business required that the [FondueMediaSource] cannot be seek at any time.
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
     * A Factory that create [FondueMediaSource] for demo purpose that use [DefaultMediaSourceFactory].
     *
     * @param context The context.
     */
    class Factory(context: Context) : PillarboxMediaSourceFactory.DelegateFactory(DefaultMediaSourceFactory(context)) {

        override fun handleMediaItem(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.uri?.host == "custom-media.ch"
        }

        override fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: MediaSource.Factory): MediaSource {
            return FondueMediaSource(mediaItem, mediaSourceFactory)
        }
    }
}
