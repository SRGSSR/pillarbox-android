/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Timeline
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SuspendMediaSource
import ch.srgssr.pillarbox.player.utils.DebugLogger
import java.net.URL

/**
 * Mime Type for representing SRG SSR content
 */
const val MimeTypeSrg = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

/**
 * MediaSource that handle SRG SSR content.
 *
 * @param mediaItem The [MediaItem] set by the user.
 * @param mediaCompositionMediaItemSource The [MediaCompositionMediaItemSource] to load SRG SSR data.
 * @param mediaSourceFactory The [MediaSource.Factory] to create the media [MediaSource], for example HlsMediaSource or DashMediaSource.
 * @param minLiveDvrDurationMs The minimal live duration to be considered DVR.
 */
class SRGMediaSource private constructor(
    mediaItem: MediaItem,
    private val mediaCompositionMediaItemSource: MediaCompositionMediaItemSource,
    private val mediaSourceFactory: MediaSource.Factory,
    private val minLiveDvrDurationMs: Long,
) : SuspendMediaSource(mediaItem) {

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        val loadedItem = mediaCompositionMediaItemSource.loadMediaItem(mediaItem)
        updateMediaItem(
            getMediaItem().buildUpon()
                .setMediaMetadata(loadedItem.mediaMetadata)
                .setTag(loadedItem.localConfiguration?.tag)
                .build()
        )
        val internalMediaItem = loadedItem.buildUpon().setMimeType(null).build()
        return mediaSourceFactory.createMediaSource(internalMediaItem)
    }

    override fun onChildSourceInfoRefreshed(
        childSourceId: String?,
        mediaSource: MediaSource,
        newTimeline: Timeline
    ) {
        DebugLogger.debug(TAG, "onChildSourceInfoRefreshed: $childSourceId")
        super.onChildSourceInfoRefreshed(childSourceId, mediaSource, SRGTimeline(minLiveDvrDurationMs, newTimeline))
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite SRGSSR needs.
     *  - Live stream with a window duration <= [minLiveDvrDurationMs] cannot seek.
     */
    private class SRGTimeline(val minLiveDvrDurationMs: Long, timeline: Timeline) : ForwardingTimeline(timeline) {

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            if (internalWindow.isLive()) {
                internalWindow.isSeekable = internalWindow.durationMs >= minLiveDvrDurationMs
            }
            return internalWindow
        }
    }

    /**
     * Factory create a [SRGMediaSource].
     *
     * @param mediaSourceFactory The [MediaSource.Factory] to create the internal [MediaSource]. By default [DefaultMediaSourceFactory].
     * @param mediaCompositionMediaItemSource The [MediaCompositionMediaItemSource] to load SRG SSR data.
     */
    class Factory(
        mediaSourceFactory: DefaultMediaSourceFactory,
        private val mediaCompositionMediaItemSource: MediaCompositionMediaItemSource = MediaCompositionMediaItemSource(
            DefaultMediaCompositionDataSource()
        )
    ) :
        PillarboxMediaSourceFactory.DelegateFactory(mediaSourceFactory) {
        /**
         * Minimal duration in milliseconds to consider a live with seek capabilities.
         */
        var minLiveDvrDurationMs = LIVE_DVR_MIN_DURATION_MS

        constructor(dataSource: DataSource.Factory, mediaCompositionMediaItemSource: MediaCompositionMediaItemSource) : this(
            DefaultMediaSourceFactory(dataSource),
            mediaCompositionMediaItemSource
        )

        constructor(
            context: Context,
            baseIlHostUrl: URL = IlHost.DEFAULT
        ) : this(
            dataSource = AkamaiTokenDataSource.Factory(defaultDataSourceFactory = DefaultDataSource.Factory(context)),
            mediaCompositionMediaItemSource = MediaCompositionMediaItemSource(DefaultMediaCompositionDataSource(baseUrl = baseIlHostUrl))
        )

        override fun handleMediaItem(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.mimeType == MimeTypeSrg || mediaItem.localConfiguration?.uri?.lastPathSegment.isValidMediaUrn()
        }

        override fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: MediaSource.Factory): MediaSource {
            return SRGMediaSource(mediaItem, mediaCompositionMediaItemSource, mediaSourceFactory, minLiveDvrDurationMs)
        }
    }

    companion object {
        private const val TAG = "SRGMediaSource"
        private const val LIVE_DVR_MIN_DURATION_MS = 60000L // 60s
    }
}
