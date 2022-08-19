/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.util.Log
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
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

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        Log.d(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        mediaItemScope = MainScope()
        mediaItemScope?.launch(exceptionHandler) {
            mediaItemSource.loadMediaItem(mediaItem).first()
            mediaItemSource.loadMediaItem(mediaItem).collectLatest { loadedItem ->
                Log.d(TAG, "collecting new mediaItem id= ${loadedItem.mediaId} title = ${loadedItem.mediaMetadata.title}")
                pendingError = null
                if (loadedMediaSource == null) {
                    mediaItem = loadedItem
                    val newMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
                    loadedMediaSource = newMediaSource
                    prepareChildSource(null, newMediaSource)
                } else {
                    // Only update metadata for the currentTimeLine
                    if (loadedTimeline != null && mediaItem != loadedItem) {
                        if (mediaItem.localConfiguration != null &&
                            mediaItem.localConfiguration!!.uri != loadedItem.localConfiguration?.uri
                        ) {
                            val loadInfo = LoadEventInfo(0, DataSpec(mediaItem.localConfiguration!!.uri), C.TIME_UNSET)
                            val mediaLoad = MediaLoadData(C.DATA_TYPE_UNKNOWN)
                            // Send error, treated as an internal error and doesn't throw a player error :(
                            createEventDispatcher(periodId)
                                .loadError(
                                    loadInfo,
                                    mediaLoad,
                                    SourceUriChangeException(mediaItem.buildUpon().build(), loadedItem),
                                    true
                                )
                        } else {
                            mediaItem = loadedItem
                            loadedTimeline?.let {
                                refreshSourceInfo(PillarboxTimeline(it, mediaItem))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun maybeThrowSourceInfoRefreshError() {
        if (pendingError != null) {
            throw IOException(pendingError)
        }
        super.maybeThrowSourceInfoRefreshError()
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
        return loadedMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    /**
     * Release period called then exoplayer switch to another item
     *
     * @param mediaPeriod
     */
    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        Log.e(TAG, "releasePeriod: $mediaPeriod")
        mediaPeriod.maybeThrowPrepareError()
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

    /**
     * Pillarbox timeline wrap the underlying Timeline and change all window with the current mediaItem
     */
    private class PillarboxTimeline(private val timeline: Timeline, private val mediaItem: MediaItem) : Timeline() {
        override fun getWindowCount(): Int {
            return timeline.windowCount
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
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
    }

    private fun handleException(exception: Throwable) {
        Log.e(TAG, "error while preparing source", exception)
        pendingError = exception
    }

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
