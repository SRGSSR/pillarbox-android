/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.media.RemoteMediaClient

internal fun RemoteMediaClient.getContentPositionMs(): Long {
    return if (approximateStreamPosition == MediaInfo.UNKNOWN_DURATION) {
        C.TIME_UNSET
    } else {
        // approximateLiveSeekableRangeStart = 0 when it is a seekable live.
        approximateStreamPosition - approximateLiveSeekableRangeStart
    }
}

internal fun RemoteMediaClient.getContentDurationMs(): Long {
    return if (isLiveStream) {
        approximateLiveSeekableRangeEnd - approximateLiveSeekableRangeStart
    } else {
        streamDuration.takeIf { it != MediaInfo.UNKNOWN_DURATION } ?: C.TIME_UNSET
    }
}

internal fun RemoteMediaClient.getPlaybackState(): @Player.State Int {
    if (mediaQueue.itemCount == 0) return Player.STATE_IDLE
    return when (playerState) {
        MediaStatus.PLAYER_STATE_IDLE, MediaStatus.PLAYER_STATE_UNKNOWN -> Player.STATE_IDLE
        MediaStatus.PLAYER_STATE_PAUSED, MediaStatus.PLAYER_STATE_PLAYING -> Player.STATE_READY
        MediaStatus.PLAYER_STATE_BUFFERING, MediaStatus.PLAYER_STATE_LOADING -> Player.STATE_BUFFERING
        else -> Player.STATE_IDLE
    }
}

internal fun RemoteMediaClient.getCurrentMediaItemIndex(): Int {
    return currentItem?.let { mediaQueue.indexOfItemWithId(it.itemId) } ?: MediaQueueItem.INVALID_ITEM_ID
}

internal fun RemoteMediaClient.getMediaIdFromIndex(index: Int): Int {
    return mediaQueue.itemIdAtIndex(index)
}

internal fun RemoteMediaClient.getRepeatMode(): @Player.RepeatMode Int {
    return when (mediaStatus?.queueRepeatMode) {
        MediaStatus.REPEAT_MODE_REPEAT_ALL,
        MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE -> Player.REPEAT_MODE_ALL

        MediaStatus.REPEAT_MODE_REPEAT_OFF -> Player.REPEAT_MODE_OFF
        MediaStatus.REPEAT_MODE_REPEAT_SINGLE -> Player.REPEAT_MODE_ONE
        else -> Player.REPEAT_MODE_OFF
    }
}

internal fun RemoteMediaClient.getVolume(): Double {
    return mediaStatus?.streamVolume ?: 0.0
}

internal fun RemoteMediaClient.isMuted(): Boolean {
    return mediaStatus?.isMute == true
}

internal fun RemoteMediaClient.getTracks(): Tracks {
    val mediaTracks = mediaInfo?.mediaTracks ?: emptyList<MediaTrack>()
    return if (mediaTracks.isEmpty()) {
        Tracks.EMPTY
    } else {
        val selectedTrackIds: LongArray = mediaStatus?.activeTrackIds ?: longArrayOf()
        val tabTrackGroup = mediaTracks.map { mediaTrack ->
            val trackGroup = mediaTrack.toTrackGroup()
            Tracks.Group(trackGroup, false, intArrayOf(C.FORMAT_HANDLED), booleanArrayOf(selectedTrackIds.contains(mediaTrack.id)))
        }
        Tracks(tabTrackGroup)
    }
}

/**
 * [MediaStatus.playbackRate] returns:
 * - 0 if it is paused.
 * - A negative value if playing backward.
 * - A positive value if playing normally.
 */
internal fun RemoteMediaClient.getPlaybackRate(): Float {
    return mediaStatus?.playbackRate?.toFloat()?.takeIf { it > 0f } ?: PlaybackParameters.DEFAULT.speed
}
