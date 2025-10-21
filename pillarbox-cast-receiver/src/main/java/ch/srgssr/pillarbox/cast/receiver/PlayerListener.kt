/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.cast.PillarboxCastUtil
import ch.srgssr.pillarbox.cast.TracksConverter
import ch.srgssr.pillarbox.cast.receiver.extensions.setPlaybackRateFromPlaybackParameter
import ch.srgssr.pillarbox.cast.receiver.extensions.setSupportedMediaCommandsFromAvailableCommand
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLiveSeekableRange
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueManager

/**
 * Synchronizes MediaManager with Player events that are not already handled with the MediaSession.
 */
internal class PlayerListener(
    val tracksConverter: TracksConverter,
    val mediaManager: MediaManager,
    val mediaQueueManager: MediaQueueManager = mediaManager.mediaQueueManager,

) : PillarboxPlayer.Listener {

    private val mediaStatusModifier = mediaManager.mediaStatusModifier
    private val window = Timeline.Window()

    override fun onTracksChanged(tracks: Tracks) {
        val tracksInfo = tracksConverter.toCastTracksInfo(tracks)
        with(mediaStatusModifier) {
            mediaInfoModifier?.mediaTracks = tracksInfo.mediaTracks
            mediaTracksModifier.setActiveTrackIds(tracksInfo.activeTrackIds)
        }
        mediaManager.broadcastMediaStatus()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        mediaQueueManager.queueRepeatMode = PillarboxCastUtil.getQueueRepeatModeFromRepeatMode(repeatMode)
        mediaManager.broadcastMediaStatus()
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(availableCommands)
        mediaManager.broadcastMediaStatus()
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        mediaStatusModifier.setPlaybackRateFromPlaybackParameter(playbackParameters)
        mediaManager.broadcastMediaStatus()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_MEDIA_ITEM_TRANSITION,
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
            if (player.currentMediaItemIndex != C.INDEX_UNSET && player.mediaItemCount > 0) {
                player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
                if (player.isCurrentMediaItemLive) {
                    val liveSeekableRange = MediaLiveSeekableRange.Builder()
                        .setIsLiveDone(false)
                        .setIsMovingWindow(window.isDynamic)
                        .setStartTime(0)
                        .setEndTime(window.durationMs)
                        .build()
                    mediaStatusModifier.liveSeekableRange = liveSeekableRange
                    mediaStatusModifier.mediaInfoModifier?.streamType = MediaInfo.STREAM_TYPE_LIVE
                    mediaStatusModifier.streamPosition = player.currentPosition
                } else {
                    mediaStatusModifier.liveSeekableRange = null
                    mediaStatusModifier.mediaInfoModifier?.streamType = MediaInfo.STREAM_TYPE_BUFFERED
                    mediaStatusModifier.streamPosition = null
                }

                val duration = if (window.durationMs == C.TIME_UNSET) null else window.durationMs
                mediaStatusModifier.mediaInfoModifier?.streamDuration = duration

                val currentId = mediaQueueManager.queueItems?.get(player.currentMediaItemIndex)?.itemId
                if (currentId != mediaQueueManager.currentItemId) {
                    mediaQueueManager.currentItemId = currentId
                    mediaStatusModifier.mediaInfoModifier?.setDataFromMediaInfo(mediaQueueManager.queueItems?.first { it.itemId == currentId }?.media)
                }
            } else {
                mediaStatusModifier.clear()
            }
            mediaManager.broadcastMediaStatus()
        }
    }
}
