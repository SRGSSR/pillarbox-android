/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.MediaItem
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking

/**
 * Pillarbox media source load a MediaItem from [mediaItem] with [mediaItemSource].
 * It use [mediaSourceFactory] to create the real underlying MediaSource playable for Exoplayer.
 *
 * @param mediaItem input mediaItem
 * @constructor Create empty Pillarbox media source
 */
abstract class SuspendMediaSource(
    private var mediaItem: MediaItem
) : CompositeMediaSource<String>() {
    private var loadedMediaSource: MediaSource? = null
    private var pendingError: Throwable? = null

    /**
     * Load media source
     *
     * @return [MediaSource]
     */
    abstract suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource

    @Suppress("TooGenericExceptionCaught")
    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        DebugLogger.debug(TAG, "prepareSourceInternal: mediaId = ${mediaItem.mediaId} on ${Thread.currentThread()}")
        pendingError = null
        // We have to use runBlocking to execute code in the same thread as prepareSourceInternal due to DRM.
        runBlocking {
            try {
                loadedMediaSource = loadMediaSource(mediaItem)
                loadedMediaSource?.let {
                    DebugLogger.debug(TAG, "prepare child source loaded mediaId = ${mediaItem.mediaId}")
                    prepareChildSource(mediaItem.mediaId, it)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
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
        val currentItemWithoutTrackerData = this.mediaItem.buildUpon().setTag(null).build()
        val mediaItemWithoutTrackerData = mediaItem.buildUpon().setTag(null).build()
        return !(
            currentItemWithoutTrackerData.mediaId != mediaItemWithoutTrackerData.mediaId ||
                currentItemWithoutTrackerData.localConfiguration != mediaItemWithoutTrackerData.localConfiguration
            )
    }

    override fun updateMediaItem(mediaItem: MediaItem) {
        this.mediaItem = mediaItem
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

    private fun handleException(exception: Throwable) {
        DebugLogger.error(TAG, "error while preparing source", exception)
        pendingError = exception
    }

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
