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
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.source.SRGMediaSource.SRGTimeline.Companion.LIVE_DVR_MIN_DURATION_MS
import ch.srgssr.pillarbox.player.source.SuspendMediaSource
import ch.srgssr.pillarbox.player.utils.DebugLogger

const val MIME_TYPE_SRG = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

class SRGMediaSource private constructor(
    mediaItem: MediaItem,
    private val mediaCompositionMediaItemSource: MediaCompositionMediaItemSource,
    private val mediaSourceFactory: MediaSource.Factory,
) : SuspendMediaSource(mediaItem) {

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        // FIXME : We could also remove clipping configuration if we want?
        // FIXME : load directly the mediaComposition here!
        val loadedItem = mediaCompositionMediaItemSource.loadMediaItem(mediaItem)
        val internalMediaItem = loadedItem.buildUpon().setMimeType(null).build()
        return mediaSourceFactory.createMediaSource(internalMediaItem)
    }

    override fun onChildSourceInfoRefreshed(
        id: String?,
        mediaSource: MediaSource,
        timeline: Timeline
    ) {
        DebugLogger.debug(TAG, "onChildSourceInfoRefreshed: $id")
        refreshSourceInfo(SRGTimeline(timeline))
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite Pillarbox needs.
     *  - Live stream with a window duration <= [LIVE_DVR_MIN_DURATION_MS] are not seekable.
     */
    private class SRGTimeline(private val timeline: Timeline) : Timeline() {
        override fun getWindowCount(): Int {
            return timeline.windowCount
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            if (internalWindow.isLive()) {
                internalWindow.isSeekable = internalWindow.durationMs >= LIVE_DVR_MIN_DURATION_MS
            }
            return internalWindow
        }

        override fun getPeriodCount(): Int {
            return timeline.periodCount
        }

        override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean): Period {
            return timeline.getPeriod(periodIndex, period, setIds)
        }

        override fun getIndexOfPeriod(uid: Any): Int {
            return timeline.getIndexOfPeriod(uid)
        }

        override fun getUidOfPeriod(periodIndex: Int): Any {
            return timeline.getUidOfPeriod(periodIndex)
        }

        companion object {
            private const val LIVE_DVR_MIN_DURATION_MS = 60000L // 60s
        }
    }

    class Factory(
        private val mediaSourceFactory: MediaSource.Factory,
        private val mediaCompositionMediaItemSource: MediaCompositionMediaItemSource
    ) :
        MediaSource.Factory by mediaSourceFactory {
        init {
            assert(mediaSourceFactory !is Factory)
        }

        constructor(dataSource: DataSource.Factory, mediaCompositionMediaItemSource: MediaCompositionMediaItemSource) : this(
            DefaultMediaSourceFactory(dataSource),
            mediaCompositionMediaItemSource
        )

        constructor(
            context: Context
        ) : this(
            dataSource = AkamaiTokenDataSource.Factory(defaultDataSourceFactory = DefaultDataSource.Factory(context)),
            mediaCompositionMediaItemSource = MediaCompositionMediaItemSource(DefaultMediaCompositionDataSource())
        )

        override fun createMediaSource(mediaItem: MediaItem): MediaSource {
            assert(mediaItem.localConfiguration != null) { "local configuration should not be null" }
            // FIXME we could also parse the uri and if last segment is an urn create the media
            if (mediaItem.localConfiguration?.mimeType == MIME_TYPE_SRG || mediaItem.localConfiguration?.uri?.lastPathSegment.isValidMediaUrn()) {
                return SRGMediaSource(mediaItem, mediaCompositionMediaItemSource, mediaSourceFactory)
            }

            return mediaSourceFactory.createMediaSource(mediaItem)
        }
    }

    companion object {
        const val TAG = "SRGMediaSource"
    }
}
