/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.util.Log
import androidx.media3.common.BuildConfig
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.PillarboxTimeline.Companion.LIVE_DVR_MIN_DURATION_MS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

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

    /**
     * Scope to execute the MediaItemSource.loadMediaItem.
     */
    private var scope: CoroutineScope? = null
    private var pendingError: Throwable? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception)
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        Log.d(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        scope = MainScope()
        scope?.launch(exceptionHandler) {
            val loadedItem = mediaItemSource.loadMediaItem(mediaItem)
            mediaItem = loadedItem
            loadedMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
            loadedMediaSource?.let {
                Log.d(TAG, "prepare child source loaded mediaId = ${loadedItem.mediaId}")
                prepareChildSource(loadedItem.mediaId, it)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun maybeThrowSourceInfoRefreshError() {
        if (pendingError != null) {
            throw IOException(pendingError)
        }
        /*
         * Sometimes Hls or Dash media source throw NullPointerException at startup with no reason
         * We decide to ignore that kind of exception during source preparation.
         */
        try {
            super.maybeThrowSourceInfoRefreshError()
        } catch (e: NullPointerException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "maybeThrowSourceInfoRefreshError", e)
        }
    }

    override fun releaseSourceInternal() {
        super.releaseSourceInternal()
        Log.d(TAG, "releaseSourceInternal")
        pendingError = null
        loadedMediaSource = null
        scope?.cancel()
        scope = null
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
        Log.d(TAG, "createPeriod: $id")
        return loadedMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        Log.d(TAG, "releasePeriod: $mediaPeriod")
        loadedMediaSource?.releasePeriod(mediaPeriod)
    }

    override fun onChildSourceInfoRefreshed(
        id: String?,
        mediaSource: MediaSource,
        timeline: Timeline
    ) {
        Log.d(TAG, "onChildSourceInfoRefreshed: $id")
        refreshSourceInfo(PillarboxTimeline(timeline))
    }

    private fun handleException(exception: Throwable) {
        Log.e(TAG, "error while preparing source", exception)
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
