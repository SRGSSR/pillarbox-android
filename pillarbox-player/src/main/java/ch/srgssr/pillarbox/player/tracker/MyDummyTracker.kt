/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import kotlin.time.Duration.Companion.milliseconds

class MyDummyTracker : MediaItemTracker {
    private enum class State { Idle, Play, Pause }

    private var urn: String = "unknown"
    private var state = State.Idle
    private val listener = object : AnalyticsListener {

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            when (state) {
                Player.STATE_ENDED -> notifyEof(eventTime.currentPlaybackPositionMs)
            }
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            if (isPlaying) {
                notifyPlaying(positionMs = eventTime.currentPlaybackPositionMs)
            } else {
                notifyPause(positionMs = eventTime.currentPlaybackPositionMs)
            }
        }

        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && oldPosition.mediaItemIndex == newPosition.mediaItemIndex) {
                notifyEof(oldPosition.positionMs)
            }
        }

        override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                notifyPlaying(positionMs = eventTime.eventPlaybackPositionMs)
            }
        }

        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
            send("Error", eventTime.currentPlaybackPositionMs)
        }
    }

    private fun notifyPlaying(positionMs: Long) {
        if (state == State.Play) return
        send("Play", positionMs)
        state = State.Play
    }

    private fun notifyPause(positionMs: Long) {
        if (state == State.Play) {
            send("Pause", positionMs)
            state = State.Pause
        }
    }

    private fun notifyEof(positionMs: Long) {
        if (state == State.Idle) return
        send("EoF", positionMs)
        state = State.Idle
    }

    private fun notifyStop(positionMs: Long) {
        if (state == State.Idle) return
        send("Stop", positionMs)
        state = State.Idle
    }

    private fun send(event: String, positionMs: Long) {
        Log.d(TAG, "[$event] @ ${positionMs.milliseconds} urn = $urn")
    }

    override fun created(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) {
        Log.d(TAG, "Created ${session.sessionId}")
    }

    override fun cleared(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) {
        Log.d(TAG, "Cleared ${session.sessionId}")
    }

    override fun start(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) {
        Log.d(TAG, "start: ${session.sessionId}")
        urn = session.mediaItem.mediaId
        player.addAnalyticsListener(listener)
        if (player.isPlaying) notifyPlaying(player.currentPosition)
    }

    override fun stop(session: PlaybackSessionManager.SessionInfo, player: PillarboxExoPlayer) {
        Log.d(TAG, "stop: ${session.session.sessionId} ${player.metricsCollector.getMetricsForSession(session.session)}")
        player.removeAnalyticsListener(listener)
        notifyStop(session.position)
    }

    override fun start(player: ExoPlayer, initialData: Any?) {
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
    }

    companion object {
        private const val TAG = "DummyTagCommander"
    }
}
