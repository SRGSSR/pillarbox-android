/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.PillarboxTimeline.Companion.LIVE_DVR_MIN_DURATION_MS
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking

/**
 * Pillarbox media source load a MediaItem from [mediaItem] with [mediaItemSource].
 * It use [mediaSourceFactory] to create the real underlying MediaSource playable for Exoplayer.
 *
 * @property mediaItem input mediaItem
 * @property mediaItemSource load asynchronously a MediaItem
 * @property mediaSourceFactory create MediaSource from a MediaItem
 * @constructor Create empty Pillarbox media source
 */
class PillarboxMediaSource(
    private var mediaItem: MediaItem,
    private val mediaItemSource: MediaItemSource,
    private val mediaSourceFactory: MediaSource.Factory
) : CompositeMediaSource<String>() {
    private var loadedMediaSource: MediaSource? = null
    private var pendingError: Throwable? = null

    @Suppress("TooGenericExceptionCaught")
    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        DebugLogger.debug(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        // We have to use runBlocking to execute code in the same thread as prepareSourceInternal due to DRM.
        runBlocking {
            try {
                val loadedItem = mediaItemSource.loadMediaItem(mediaItem)
                mediaItem = loadedItem
                loadedMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
                loadedMediaSource?.let {
                    DebugLogger.debug(TAG, "prepare child source loaded mediaId = ${loadedItem.mediaId}")
                    prepareChildSource(loadedItem.mediaId, it)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun maybeThrowSourceInfoRefreshError() {
        pendingError?.let {
            throw it
        }
        /*
         * Sometimes Hls or Dash media source throw NullPointerException at startup with no reason
         * We decide to ignore that kind of exception during source preparation.
         */
        try {
            super.maybeThrowSourceInfoRefreshError()
        } catch (e: NullPointerException) {
            DebugLogger.error(TAG, "maybeThrowSourceInfoRefreshError", e)
        }
    }

    override fun releaseSourceInternal() {
        super.releaseSourceInternal()
        DebugLogger.debug(TAG, "releaseSourceInternal")
        pendingError = null
        loadedMediaSource = null
    }

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    @Suppress("UnsafeCallOnNullableType")
    override fun createPeriod(
        id: MediaSource.MediaPeriodId,
        allocator: Allocator,
        startPositionUs: Long
    ): MediaPeriod {
        DebugLogger.debug(TAG, "createPeriod: $id")
        return loadedMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        DebugLogger.debug(TAG, "releasePeriod: $mediaPeriod")
        loadedMediaSource?.releasePeriod(mediaPeriod)
    }

    override fun onChildSourceInfoRefreshed(
        id: String?,
        mediaSource: MediaSource,
        timeline: Timeline
    ) {
        DebugLogger.debug(TAG, "onChildSourceInfoRefreshed: $id")
        refreshSourceInfo(PillarboxTimeline(timeline))
    }

    private fun handleException(exception: Throwable) {
        DebugLogger.error(TAG, "error while preparing source", exception)
        pendingError = exception
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite Pillarbox needs.
     *  - Live stream with a window duration <= [LIVE_DVR_MIN_DURATION_MS] are not seekable.
     */
    private class PillarboxTimeline(private val timeline: Timeline) : Timeline() {
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

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
