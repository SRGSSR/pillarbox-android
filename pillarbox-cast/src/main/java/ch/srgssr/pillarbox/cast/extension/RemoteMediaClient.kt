/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

@file:Suppress("TooManyFunctions")

package ch.srgssr.pillarbox.cast.extension

import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS
import androidx.media3.common.Player.COMMAND_CHANGE_MEDIA_ITEMS
import androidx.media3.common.Player.COMMAND_GET_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_GET_TIMELINE
import androidx.media3.common.Player.COMMAND_GET_TRACKS
import androidx.media3.common.Player.COMMAND_GET_VOLUME
import androidx.media3.common.Player.COMMAND_PLAY_PAUSE
import androidx.media3.common.Player.COMMAND_RELEASE
import androidx.media3.common.Player.COMMAND_SEEK_BACK
import androidx.media3.common.Player.COMMAND_SEEK_FORWARD
import androidx.media3.common.Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_DEFAULT_POSITION
import androidx.media3.common.Player.COMMAND_SEEK_TO_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SET_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SET_PLAYLIST_METADATA
import androidx.media3.common.Player.COMMAND_SET_REPEAT_MODE
import androidx.media3.common.Player.COMMAND_SET_SHUFFLE_MODE
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import androidx.media3.common.Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS
import androidx.media3.common.Player.COMMAND_SET_VOLUME
import androidx.media3.common.Player.COMMAND_STOP
import androidx.media3.common.Tracks
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.media.RemoteMediaClient

internal val PERMANENT_AVAILABLE_COMMANDS = Player.Commands.Builder()
    .addAll(
        COMMAND_PLAY_PAUSE,
        COMMAND_GET_CURRENT_MEDIA_ITEM,
        COMMAND_GET_TIMELINE,
        COMMAND_STOP,
        COMMAND_RELEASE,
        COMMAND_SET_MEDIA_ITEM,
        COMMAND_CHANGE_MEDIA_ITEMS,
        COMMAND_SET_SHUFFLE_MODE,
        COMMAND_SET_REPEAT_MODE,
        COMMAND_GET_VOLUME,
        COMMAND_GET_TRACKS,
        COMMAND_SET_PLAYLIST_METADATA,
    )
    .build()

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

internal fun RemoteMediaClient.getAvailableCommands(
    seekBackIncrementMs: Long,
    seekForwardIncrementMs: Long,
): Player.Commands {
    val contentPositionMs = getContentPositionMs()
    val contentDurationMs = getContentDurationMs()
    val isCommandSupported = { command: Long -> mediaStatus?.isMediaCommandSupported(command) == true }
    val currentItemIndex = getCurrentMediaItemIndex()
    val isPlayingAd = mediaStatus?.isPlayingAd == true
    val itemCount = mediaQueue.itemCount
    val hasNextItem = !isPlayingAd && currentItemIndex + 1 < itemCount
    val hasPreviousItem = !isPlayingAd && currentItemIndex - 1 >= 0
    val canSeek = itemCount > 0 && !isPlayingAd && isCommandSupported(MediaStatus.COMMAND_SEEK) && contentDurationMs != C.TIME_UNSET
    val canSeekBack = canSeek && contentPositionMs != C.TIME_UNSET && contentPositionMs - seekBackIncrementMs > 0
    val canSeekForward = canSeek && contentPositionMs + seekForwardIncrementMs < contentDurationMs

    return PERMANENT_AVAILABLE_COMMANDS.buildUpon()
        .addIf(COMMAND_SET_TRACK_SELECTION_PARAMETERS, isCommandSupported(MediaStatus.COMMAND_EDIT_TRACKS))
        .addIf(COMMAND_SEEK_TO_DEFAULT_POSITION, !isPlayingAd)
        .addIf(COMMAND_SEEK_TO_MEDIA_ITEM, !isPlayingAd)
        .addIf(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM, hasNextItem)
        .addIf(COMMAND_SEEK_TO_NEXT, hasNextItem || canSeek)
        .addIf(COMMAND_SEEK_TO_PREVIOUS, hasPreviousItem || canSeek)
        .addIf(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM, hasPreviousItem)
        .addIf(COMMAND_SET_VOLUME, isCommandSupported(MediaStatus.COMMAND_SET_VOLUME))
        .addIf(COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS, isCommandSupported(MediaStatus.COMMAND_TOGGLE_MUTE))
        .addIf(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, canSeek)
        .addIf(COMMAND_SEEK_BACK, canSeekBack)
        .addIf(COMMAND_SEEK_FORWARD, canSeekForward)
        .addIf(COMMAND_SET_SPEED_AND_PITCH, isCommandSupported(MediaStatus.COMMAND_PLAYBACK_RATE))
        .build()
}
