/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLiveSeekableRange
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaStatusWriter

/**
 * An interceptor that log only the final [MediaStatus].
 */
object LogMediaStatusInterceptor : MediaManager.MediaStatusInterceptor {
    private const val TAG = "MediaStatusInterceptor"

    override fun intercept(mediaStatusWriter: MediaStatusWriter) {
        val mediaStatus = mediaStatusWriter.mediaStatus
        with(mediaStatus) {
            Log.d(TAG, "MediaStatus:")
            Log.d(TAG, "  currentItemId = ${this.currentItemId}")
            Log.d(TAG, "  queueItems = #$queueItemCount - ${queueItems.map { it.toPrettyString() }}")
            Log.d(TAG, "  queueData = ${queueData?.prettyString()}")
            Log.d(TAG, "  state = ${playbackStateString(playerState)}")
            Log.d(TAG, "  streamPosition = $streamPosition")
            Log.d(TAG, "  seekableLiveRange = ${liveSeekableRange?.prettyString()}")
            Log.d(TAG, "  playbackRate = $playbackRate")
            Log.d(TAG, "  mediaInfo = ${mediaInfo?.prettyString()}")
            Log.d(TAG, "--------------------------------------------------------------------------")
        }
    }

    private fun playbackStateString(playbackState: Int): String {
        return when (playbackState) {
            MediaStatus.PLAYER_STATE_IDLE -> "PLAYER_STATE_IDLE"
            MediaStatus.PLAYER_STATE_BUFFERING -> "PLAYER_STATE_BUFFERING"
            MediaStatus.PLAYER_STATE_LOADING -> "PLAYER_STATE_LOADING"
            MediaStatus.PLAYER_STATE_PAUSED -> "PLAYER_STATE_PAUSED"
            MediaStatus.PLAYER_STATE_PLAYING -> "PLAYER_STATE_PLAYING"
            else -> "PLAYER_STATE_UNKNOWN"
        }
    }

    private fun streamTypeString(streamType: Int): String {
        return when (streamType) {
            MediaInfo.STREAM_TYPE_BUFFERED -> "STREAM_TYPE_BUFFERED"
            MediaInfo.STREAM_TYPE_LIVE -> "STREAM_TYPE_LIVE"
            MediaInfo.STREAM_TYPE_NONE -> "STREAM_TYPE_NONE"
            MediaInfo.STREAM_TYPE_INVALID -> "STREAM_TYPE_INVALID"
            else -> "Invalid streamType value"
        }
    }

    private fun MediaQueueData.prettyString(): String {
        return "MediaQueue name = $name items = ${items?.map { it.toPrettyString() }}"
    }

    private fun MediaQueueItem.toPrettyString(): String {
        return "QueueItem[$itemId] Media = contentId = ${media?.contentId} contentUrl = ${media?.contentUrl} metadata = ${
            media?.metadata?.getString(
                MediaMetadata.KEY_TITLE
            )
        }"
    }

    private fun MediaInfo.prettyString(): String {
        return "contentId = $contentId contentUrl = $contentUrl metadata = ${metadata?.prettyString()} streamDuration = $streamDuration ${
            streamTypeString(
                streamType
            )
        }"
    }

    private fun MediaMetadata.prettyString(): String {
        return "title = ${getString(MediaMetadata.KEY_TITLE)}"
    }

    private fun MediaLiveSeekableRange.prettyString(): String {
        return "isMovingWindow = $isMovingWindow [$startTime - $endTime] isLiveDone = $isLiveDone"
    }
}
