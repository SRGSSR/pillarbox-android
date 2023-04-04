/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

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
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.PillarboxTimeline.Companion.LIVE_DVR_MIN_DURATION_MS
import ch.srgssr.pillarbox.player.utils.DebugLogger
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
    private var loadedTimeline: Timeline? = null
    private var periodId: MediaSource.MediaPeriodId? = null
    private val itemDataFlow = mediaItemSource.loadMediaItem(mediaItem)
    private var updateMediaItemJob: Job? = null

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
        DebugLogger.debug(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        scope = MainScope()
        scope?.launch(exceptionHandler) {
            val loadedItem = itemDataFlow.first()
            mediaItem = loadedItem
            val newMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
            loadedMediaSource = newMediaSource
            loadedMediaSource?.let {
                DebugLogger.debug(TAG, "prepare child source loaded mediaId = ${loadedItem.mediaId}")
                prepareChildSource(loadedItem.mediaId, it)
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
        scope?.cancel()
        scope = null
        pendingError = null
        loadedMediaSource = null
        loadedMediaSource = null
        loadedTimeline = null
        periodId = null
        updateMediaItemJob?.cancel()
        updateMediaItemJob = null
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
        this.periodId = id
        startCollectingFlow()
        return loadedMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        DebugLogger.debug(TAG, "releasePeriod: $mediaPeriod")
        stopCollectingFlow()
        loadedMediaSource?.releasePeriod(mediaPeriod)
    }

    private fun startCollectingFlow() {
        if (updateMediaItemJob != null) {
            stopCollectingFlow()
        }
        updateMediaItemJob = scope?.launch {
            itemDataFlow
                .catch { sendError(IOException(it)) }
                .collectLatest {
                    updateTimeLine(it)
                }
        }
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

    private fun sendError(ioException: IOException) {
        val loadInfo = LoadEventInfo(0, DataSpec(mediaItem.localConfiguration!!.uri), C.TIME_UNSET)
        val mediaLoad = MediaLoadData(C.DATA_TYPE_UNKNOWN)
        // Send error, treated as an internal error and doesn't throw a player error!
        createEventDispatcher(periodId)
            .loadError(
                loadInfo,
                mediaLoad,
                ioException,
                true
            )
    }

    private fun stopCollectingFlow() {
        updateMediaItemJob?.cancel()
    }

    override fun onChildSourceInfoRefreshed(
        id: String?,
        mediaSource: MediaSource,
        timeline: Timeline
    ) {
        DebugLogger.debug(TAG, "onChildSourceInfoRefreshed: $id")
        loadedTimeline = timeline
        refreshSourceInfo(PillarboxTimeline(timeline, mediaItem))
    }

    private fun handleException(exception: Throwable) {
        DebugLogger.error(TAG, "error while preparing source", exception)
        pendingError = exception
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite Pillarbox needs.
     *  - Live stream with a window duration <= [LIVE_DVR_MIN_DURATION_MS] are not seekable.
     */
    private class PillarboxTimeline(private val timeline: Timeline, private val mediaItem: MediaItem) : Timeline() {
        override fun getWindowCount(): Int {
            return timeline.windowCount
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            internalWindow.mediaItem = mediaItem
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
