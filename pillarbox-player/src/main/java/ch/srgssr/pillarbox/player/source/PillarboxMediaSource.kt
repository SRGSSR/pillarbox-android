/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.util.Log
import androidx.media3.common.BuildConfig
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.data.MediaItemSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Pillarbox media source load a MediaItem from [mediaItem] with [mediaItemSource].
 * It use [mediaSourceFactory] to create the real underlying MediaSource playable for Exoplayer.
 * Will update propertly any MediaItem changes.
 *
 * MediaSource isn't player state aware. Stoping the continous playback when player doesn't play doesnt work here.
 * Periodic update will be stopped when the MediaSource is released. May happens when calling Exoplayer#release.
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
) : CompositeMediaSource<Any>() {
    private var loadedMediaSource: MediaSource? = null
    private var loadedTimeline: Timeline? = null
    private var periodId: MediaSource.MediaPeriodId? = null

    /**
     * Scope to execute the MediaItemSource.loadMediaItem.
     */
    private var mediaItemScope: CoroutineScope? = null
    private var pendingError: Throwable? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception)
    }
    private val itemDataFlow = mediaItemSource.loadMediaItem(mediaItem)
    private var collectJob: Job? = null

    private fun sendError(ioException: IOException) {
        val loadInfo = LoadEventInfo(0, DataSpec(mediaItem.localConfiguration!!.uri), C.TIME_UNSET)
        val mediaLoad = MediaLoadData(C.DATA_TYPE_UNKNOWN)
        // Send error, treated as an internal error and doesn't throw a player error :(
        createEventDispatcher(periodId)
            .loadError(
                loadInfo,
                mediaLoad,
                ioException,
                true
            )
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        Log.d(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        mediaItemScope = MainScope()
        mediaItemScope?.launch(exceptionHandler) {
            val loadedItem = itemDataFlow.first()
            mediaItem = loadedItem
            val newMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
            loadedMediaSource = newMediaSource
            prepareChildSource(null, newMediaSource)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun maybeThrowSourceInfoRefreshError() {
        if (pendingError != null) {
            throw IOException(pendingError)
        }
        /*
         * Sometimes Hls content throw error at startup with no reason
         * androidx.media3.exoplayer.hls.playlist.DefaultHlsPlaylistTracker$MediaPlaylistBundle.maybeThrowPlaylistRefreshError()
         */
        try {
            super.maybeThrowSourceInfoRefreshError()
        } catch (e: NullPointerException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "maybeThrowSourceInfoRefreshError", e)
        }
    }

    override fun releaseSourceInternal() {
        super.releaseSourceInternal()
        Log.e(TAG, "releaseSourceInternal")
        mediaItemScope?.cancel()
        mediaItemScope = null
        pendingError = null
        loadedMediaSource = null
        loadedTimeline = null
        periodId = null
        collectJob = null
    }

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    /**
     * Create period when buffering the media or after seeking.
     */
    @Suppress("UnsafeCallOnNullableType")
    override fun createPeriod(
        id: MediaSource.MediaPeriodId,
        allocator: Allocator,
        startPositionUs: Long
    ): MediaPeriod {
        Log.e(TAG, "createPeriod: $id ${loadedMediaSource != null}")
        this.periodId = id
        startCollectingFlow()
        return loadedMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    /**
     * Release period called then exoplayer switch to another item
     *
     * @param mediaPeriod
     */
    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        Log.e(TAG, "releasePeriod: $mediaPeriod")
        stopCollectingFlow()
        loadedMediaSource?.releasePeriod(mediaPeriod)
    }

    override fun onChildSourceInfoRefreshed(
        id: Any?,
        mediaSource: MediaSource,
        timeline: Timeline
    ) {
        loadedTimeline = timeline
        refreshSourceInfo(PillarboxTimeline(timeline, mediaItem))
    }

    private fun startCollectingFlow() {
        if (collectJob != null) {
            stopCollectingFlow()
        }
        collectJob = mediaItemScope?.launch {
            itemDataFlow
                .catch { sendError(IOException(it)) }
                .collectLatest {
                    updateTimeLine(it)
                }
        }
    }

    private fun stopCollectingFlow() {
        collectJob?.cancel()
    }

    private fun updateTimeLine(loadedItem: MediaItem) {
        if (loadedTimeline != null && mediaItem != loadedItem) {
            if (mediaItem.localConfiguration != null &&
                mediaItem.localConfiguration!!.uri != loadedItem.localConfiguration?.uri
            ) {
                sendError(SourceUriChangeException(mediaItem, loadedItem))
            } else {
                mediaItem = loadedItem
                loadedTimeline?.let {
                    refreshSourceInfo(PillarboxTimeline(it, mediaItem))
                }
            }
        }
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline and change all window with the current mediaItem
     * For live stream with a too small window duration, it will be considered not seekable.
     */
    private class PillarboxTimeline(private val timeline: Timeline, private val mediaItem: MediaItem) : Timeline() {
        override fun getWindowCount(): Int {
            return timeline.windowCount
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            // Live window with window duration less than LIVE_DVR_MIN_DURATION_MS cannot be seekable (Live only)
            if (internalWindow.isLive()) {
                internalWindow.isSeekable = internalWindow.durationMs >= LIVE_DVR_MIN_DURATION_MS
            }
            internalWindow.mediaItem = mediaItem
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

    private fun handleException(exception: Throwable) {
        Log.e(TAG, "error while preparing source", exception)
        pendingError = exception
    }

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
