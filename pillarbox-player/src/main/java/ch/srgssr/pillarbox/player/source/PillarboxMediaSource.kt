/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TimelineWithUpdatedMediaItem
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.extension.setTrackerData
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking

/**
 * Pillarbox media source
 *
 * @param mediaItem The [MediaItem] to used for the assetLoader.
 * @param assetLoader The [AssetLoader] to used to load the source.
 * @param minLiveDvrDurationMs Minimal duration in milliseconds to consider a live with seek capabilities.
 * @constructor Create empty Pillarbox media source
 */
class PillarboxMediaSource internal constructor(
    private var mediaItem: MediaItem,
    private val assetLoader: AssetLoader,
    private val minLiveDvrDurationMs: Long,
) : CompositeMediaSource<Unit>() {
    private lateinit var mediaSource: MediaSource
    private var pendingError: Throwable? = null

    @Suppress("TooGenericExceptionCaught")
    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        DebugLogger.debug(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        // We have to use runBlocking to execute code in the same thread as prepareSourceInternal due to DRM.
        runBlocking {
            try {
                val asset = assetLoader.loadAsset(mediaItem)
                DebugLogger.debug(TAG, "Asset(${mediaItem.localConfiguration?.uri}) : ${asset.trackersData}")
                mediaSource = asset.mediaSource
                mediaItem = mediaItem.buildUpon()
                    .setMediaMetadata(asset.mediaMetaData)
                    .setTrackerData(asset.trackersData)
                    .build()
                prepareChildSource(Unit, mediaSource)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun onChildSourceInfoRefreshed(childSourceId: Unit?, mediaSource: MediaSource, newTimeline: Timeline) {
        refreshSourceInfo(SRGTimeline(minLiveDvrDurationMs, TimelineWithUpdatedMediaItem(newTimeline, getMediaItem())))
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
        return !(
            currentItemWithoutTag.mediaId != mediaItemWithoutTag.mediaId &&
                currentItemWithoutTag.localConfiguration != mediaItemWithoutTag.localConfiguration
            )
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

    @Suppress("UnsafeCallOnNullableType")
    override fun createPeriod(
        id: MediaSource.MediaPeriodId,
        allocator: Allocator,
        startPositionUs: Long
    ): MediaPeriod {
        DebugLogger.debug(TAG, "createPeriod: $id")
        return mediaSource.createPeriod(id, allocator, startPositionUs)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        DebugLogger.debug(TAG, "releasePeriod: $mediaPeriod")
        mediaSource.releasePeriod(mediaPeriod)
    }

    private fun handleException(exception: Throwable) {
        DebugLogger.error(TAG, "error while preparing source", exception)
        pendingError = exception
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

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
