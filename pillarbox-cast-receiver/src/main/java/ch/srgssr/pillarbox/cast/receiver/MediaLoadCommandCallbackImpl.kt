/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.Player
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * Handle load media request from sender.
 *
 * @link https://developers.google.com/android/reference/com/google/android/gms/cast/tv/media/MediaLoadCommandCallback
 */
internal class MediaLoadCommandCallbackImpl(
    private val mediaManager: MediaManager,
    private val player: Player,
    private val mediaItemConverter: MediaItemConverter,
) : MediaLoadCommandCallback() {

    override fun onLoad(senderId: String?, loadRequest: MediaLoadRequestData): Task<MediaLoadRequestData?> {
        Log.d(TAG, "onLoad from $senderId #items = ${loadRequest.queueData?.items?.size} startIndex = ${loadRequest.queueData?.startIndex}")
        /*
         * setDataFromLoad configures
         * - MediaQueueManager and setup MediaQueueItem ids
         * - MediaStatus is also modified data from the loadRequest.
         */
        mediaManager.setDataFromLoad(loadRequest)
        loadRequest.queueData?.let { queueData ->
            val positionMs = if (loadRequest.currentTime < 0) C.TIME_UNSET else loadRequest.currentTime
            val startIndex = if (queueData.startIndex < 0) C.INDEX_UNSET else queueData.startIndex
            player.setMediaItems(queueData.items.orEmpty().map(mediaItemConverter::toMediaItem), startIndex, positionMs)
        } ?: loadRequest.mediaInfo?.let { mediaInfo ->
            val mediaQueueItem = MediaQueueItem.Builder(mediaInfo)
                .build()
            val positionMs = if (loadRequest.currentTime < 0) C.TIME_UNSET else loadRequest.currentTime
            player.setMediaItem(mediaItemConverter.toMediaItem(mediaQueueItem), positionMs)
        }
        val playbackRate = loadRequest.playbackRate.toFloat()
        if (playbackRate > 0f) {
            player.setPlaybackSpeed(playbackRate)
        }
        player.playWhenReady = loadRequest.autoplay == true
        player.prepare()
        return Tasks.forResult(loadRequest)
    }

    companion object {
        private const val TAG = "MediaLoadCallback"
    }
}
