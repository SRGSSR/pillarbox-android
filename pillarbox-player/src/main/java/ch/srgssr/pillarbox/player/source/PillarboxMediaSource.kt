/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.player.data.MediaItemSource
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

    @Suppress("TooGenericExceptionCaught")
    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        Log.d(TAG, "prepareSourceInternal: ${mediaItem.mediaId} - ${Thread.currentThread()}")
        scope = MainScope()
        scope?.launch {
            try {
                val loadedItem = mediaItemSource.loadMediaItem(mediaItem)
                mediaItem = loadedItem
                loadedMediaSource = mediaSourceFactory.createMediaSource(loadedItem)
                loadedMediaSource?.let {
                    Log.d(TAG, "prepare child source with ${loadedItem.mediaId}")
                    pendingError = null
                    prepareChildSource(loadedItem.mediaId, it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "error while preparing source", e)
                pendingError = e
            }
        }
    }

    override fun maybeThrowSourceInfoRefreshError() {
        super.maybeThrowSourceInfoRefreshError()
        if (pendingError != null) {
            throw IOException(pendingError)
        }
    }

    override fun releaseSourceInternal() {
        super.releaseSourceInternal()
        Log.d(TAG, "releaseSourceInternal")
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
        refreshSourceInfo(timeline)
    }

    companion object {
        private const val TAG = "PillarboxMediaSource"
    }
}
