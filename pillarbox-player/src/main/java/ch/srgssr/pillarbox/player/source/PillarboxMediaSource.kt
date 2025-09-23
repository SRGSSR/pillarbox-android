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
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.hls.HlsManifest
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TimelineWithUpdatedMediaItem
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A custom [MediaSource] that wraps another [MediaSource] to provide:
 * - Flexible asset loading via an [AssetLoader].
 * - Load event handling (started, completed, error).
 *
 * @param mediaItem The [MediaItem] to load.
 * @param assetLoader The [AssetLoader] used to load the asset.
 * @param seekableLiveConfig The [SeekableLiveConfig] used to determine if the player can seek when playing live stream.
 * @param timeSource The [TimeSource] for generating timestamps for load events.
 */
class PillarboxMediaSource internal constructor(
    private var mediaItem: MediaItem,
    private val assetLoader: AssetLoader,
    private val seekableLiveConfig: SeekableLiveConfig,
    private val timeSource: TimeSource,
) : CompositeMediaSource<Unit>() {
    private lateinit var mediaSource: MediaSource
    private var pendingError: Throwable? = null
    private val eventDispatcher by lazy { createEventDispatcher(null) }
    private var loadTaskId = 0L
    private var timeMarkLoadStart: TimeMark? = null
    private var mediaItemTrackerData: MediaItemTrackerData = MutableMediaItemTrackerData.EMPTY.toMediaItemTrackerData()
    private var pillarboxMetadata: PillarboxMetadata = PillarboxMetadata.EMPTY

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
                pillarboxMetadata = asset.pillarboxMetadata
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
        refreshSourceInfo(
            PillarboxTimeline(
                timeline = TimelineWithUpdatedMediaItem(newTimeline, mediaItem),
                seekableLiveConfig = seekableLiveConfig
            )
        )
    }

    /**
     * Checks whether the [MediaItem] can be updated without reloading the media source.
     *
     * TODO Test when using MediaController or MediaBrowser.
     *
     * @param mediaItem The new [MediaItem].
     * @return Whether the [MediaItem] can be updated without reloading the media source.
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
        return PillarboxMediaPeriod(mediaPeriod = mediaSource.createPeriod(id, allocator, startPositionUs), mediaItemTrackerData, pillarboxMetadata)
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
     * Wrap the underlying [Timeline] to apply [seekableLiveConfig].
     */
    private class PillarboxTimeline(timeline: Timeline, private val seekableLiveConfig: SeekableLiveConfig) : ForwardingTimeline(timeline) {

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            val manifest: Any? = internalWindow.manifest
            if (internalWindow.isLive) {
                if (seekableLiveConfig.minHlsChunkCount > 0 && manifest is HlsManifest && window.durationMs != C.TIME_UNSET) {
                    internalWindow.isSeekable = window.durationUs > seekableLiveConfig.minHlsChunkCount * manifest.mediaPlaylist.targetDurationUs
                }
                if (seekableLiveConfig.minDashTimeShiftMs > 0 && manifest is DashManifest && manifest.timeShiftBufferDepthMs != C.TIME_UNSET) {
                    internalWindow.isSeekable = manifest.timeShiftBufferDepthMs > seekableLiveConfig.minDashTimeShiftMs
                }
            }
            return internalWindow
        }
    }

    private fun dispatchLoadStarted() {
        loadTaskId = LoadEventInfo.getNewId()
        timeMarkLoadStart = timeSource.markNow()

        eventDispatcher.loadStarted(createLoadEventInfo(), DATA_TYPE_CUSTOM_ASSET, 0)
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

    companion object {
        /**
         * A data type for SRG SSR assets.
         */
        const val DATA_TYPE_CUSTOM_ASSET = C.DATA_TYPE_CUSTOM_BASE + 1
        private const val TAG = "PillarboxMediaSource"

        /**
         * [Format.sampleMimeType] used to define Pillarbox trackers data custom tracks.
         */
        internal const val PILLARBOX_TRACKERS_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox-trackers"

        /**
         * This track type is used to identify tracks containing Pillarbox trackers data.
         */
        const val TRACK_TYPE_PILLARBOX_TRACKERS = C.DATA_TYPE_CUSTOM_BASE + 1

        init {
            MimeTypes.registerCustomMimeType(PILLARBOX_TRACKERS_MIME_TYPE, "pillarbox", TRACK_TYPE_PILLARBOX_TRACKERS)
        }
    }
}
