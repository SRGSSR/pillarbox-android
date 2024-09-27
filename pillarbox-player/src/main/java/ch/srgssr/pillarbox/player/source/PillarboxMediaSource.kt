/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Timeline
import androidx.media3.common.TrackGroup
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TimelineWithUpdatedMediaItem
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Pillarbox media source
 *
 * @param mediaItem The [MediaItem] to used for the assetLoader.
 * @param assetLoader The [AssetLoader] to used to load the source.
 * @param minLiveDvrDurationMs Minimal duration in milliseconds to consider a live with seek capabilities.
 * @param timeSource The [TimeSource].
 * @constructor Create empty Pillarbox media source
 */
class PillarboxMediaSource internal constructor(
    private var mediaItem: MediaItem,
    private val assetLoader: AssetLoader,
    private val minLiveDvrDurationMs: Long,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : CompositeMediaSource<Unit>() {
    private lateinit var mediaSource: MediaSource
    private var pendingError: Throwable? = null
    private val eventDispatcher by lazy { createEventDispatcher(null) }
    private var loadTaskId = 0L
    private var timeMarkLoadStart: TimeMark? = null
    private var mediaItemTrackerData: MediaItemTrackerData = MutableMediaItemTrackerData.EMPTY.toMediaItemTrackerData()
    private var blockedTimeRanges: List<BlockedTimeRange> = emptyList()

    @Suppress("TooGenericExceptionCaught")
    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        dispatchLoadStarted()
        super.prepareSourceInternal(mediaTransferListener)
        DebugLogger.debug(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        // We have to use runBlocking to execute code in the same thread as prepareSourceInternal due to DRM.
        runBlocking {
            try {
                val asset = assetLoader.loadAsset(mediaItem)
                dispatchLoadCompleted()
                DebugLogger.debug(TAG, "Asset(${mediaItem.localConfiguration?.uri}) : ${asset.trackersData}")
                mediaSource = asset.mediaSource
                mediaItemTrackerData = asset.trackersData
                blockedTimeRanges = asset.blockedTimeRanges
                mediaItem = mediaItem.buildUpon()
                    .setMediaMetadata(asset.mediaMetadata)
                    .build()
                prepareChildSource(Unit, mediaSource)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun onChildSourceInfoRefreshed(childSourceId: Unit?, mediaSource: MediaSource, newTimeline: Timeline) {
        refreshSourceInfo(PillarboxTimeline(minLiveDvrDurationMs, TimelineWithUpdatedMediaItem(newTimeline, getMediaItem())))
    }

    /**
     * Can update media item
     *
     * TODO Test when using MediaController or MediaBrowser.
     *
     * @param mediaItem The new mediaItem, this method is called when we replace media item.
     * @return true if the media can be update without reloading the media source.
     */
    override fun canUpdateMediaItem(mediaItem: MediaItem): Boolean {
        val currentItemWithoutTag = this.mediaItem.buildUpon().setTag(null).build()
        val mediaItemWithoutTag = mediaItem.buildUpon().setTag(null).build()
        return currentItemWithoutTag.mediaId == mediaItemWithoutTag.mediaId &&
            currentItemWithoutTag.localConfiguration == mediaItemWithoutTag.localConfiguration
    }

    override fun updateMediaItem(mediaItem: MediaItem) {
        this.mediaItem = this.mediaItem.buildUpon()
            .setMediaMetadata(mediaItem.mediaMetadata)
            .build()
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
    }

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun createPeriod(
        id: MediaSource.MediaPeriodId,
        allocator: Allocator,
        startPositionUs: Long
    ): MediaPeriod {
        DebugLogger.debug(TAG, "createPeriod: $id")
        return PillarboxMediaPeriod(mediaPeriod = mediaSource.createPeriod(id, allocator, startPositionUs), mediaItemTrackerData, blockedTimeRanges)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        DebugLogger.debug(TAG, "releasePeriod: $mediaPeriod")
        if (mediaPeriod is PillarboxMediaPeriod) {
            mediaPeriod.release(mediaSource)
        } else {
            mediaSource.releasePeriod(mediaPeriod)
        }
    }

    private fun handleException(exception: Throwable) {
        DebugLogger.error(TAG, "error while preparing source", exception)
        dispatchLoadError(exception)
        pendingError = exception
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite SRGSSR needs.
     *  - Live stream with a window duration <= [minLiveDvrDurationMs] cannot seek.
     */
    private class PillarboxTimeline(val minLiveDvrDurationMs: Long, timeline: Timeline) : ForwardingTimeline(timeline) {

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            if (internalWindow.isLive) {
                internalWindow.isSeekable = internalWindow.durationMs >= minLiveDvrDurationMs
            }
            return internalWindow
        }
    }

    private fun dispatchLoadStarted() {
        loadTaskId = LoadEventInfo.getNewId()
        timeMarkLoadStart = timeSource.markNow()

        eventDispatcher.loadStarted(createLoadEventInfo(), DATA_TYPE_CUSTOM_ASSET)
    }

    private fun dispatchLoadCompleted() {
        val startTimeMark = timeMarkLoadStart ?: return

        eventDispatcher.loadCompleted(createLoadEventInfo(startTimeMark), DATA_TYPE_CUSTOM_ASSET)

        loadTaskId = 0L
        timeMarkLoadStart = null
    }

    private fun dispatchLoadError(exception: Throwable) {
        val startTimeMark = timeMarkLoadStart ?: return

        eventDispatcher.loadError(createLoadEventInfo(startTimeMark = startTimeMark), DATA_TYPE_CUSTOM_ASSET, IOException(exception), false)

        loadTaskId = 0L
        timeMarkLoadStart = null
    }

    private fun createLoadEventInfo(startTimeMark: TimeMark? = null): LoadEventInfo {
        val currentTimeMark = timeSource.markNow()
        val mediaUri = mediaItem.localConfiguration?.uri ?: Uri.EMPTY

        return LoadEventInfo(
            loadTaskId,
            DataSpec(mediaUri),
            mediaUri,
            emptyMap(),
            currentTimeMark.elapsedNow().inWholeMilliseconds,
            startTimeMark?.let { (it.elapsedNow() - currentTimeMark.elapsedNow()).inWholeMilliseconds } ?: 0L,
            0L,
        )
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Data type for SRG SSR assets.
         */
        const val DATA_TYPE_CUSTOM_ASSET = C.DATA_TYPE_CUSTOM_BASE + 1
        private const val TAG = "PillarboxMediaSource"

        /**
         * [Format.sampleMimeType] used to define Pillarbox trackers data custom tracks.
         */
        internal const val PILLARBOX_TRACKERS_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox-trackers"

        /**
         * [Format.sampleMimeType] used to define Pillarbox blocked time interval custom tracks.
         */
        internal const val PILLARBOX_BLOCKED_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox-blocked"

        /**
         * [TrackGroup.type] for [Format]s with mime type [PILLARBOX_TRACKERS_MIME_TYPE].
         */
        const val TRACK_TYPE_PILLARBOX_TRACKERS = C.DATA_TYPE_CUSTOM_BASE + 1

        /**
         * [TrackGroup.type] for [Format]s with mime type [PILLARBOX_BLOCKED_MIME_TYPE].
         */
        const val TRACK_TYPE_PILLARBOX_BLOCKED = TRACK_TYPE_PILLARBOX_TRACKERS + 1

        init {
            MimeTypes.registerCustomMimeType(PILLARBOX_TRACKERS_MIME_TYPE, "pillarbox", TRACK_TYPE_PILLARBOX_TRACKERS)
            MimeTypes.registerCustomMimeType(PILLARBOX_BLOCKED_MIME_TYPE, "pillarbox", TRACK_TYPE_PILLARBOX_BLOCKED)
        }
    }
}
