/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.qos.QoSEventsDispatcher.Listener
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil

/**
 * Pillarbox provided implementation of [QoSEventsDispatcher].
 */
class PillarboxEventsDispatcher(
    private val sessionManager: PlaybackSessionManager,
) : QoSEventsDispatcher {
    private val analyticsListener = EventsDispatcherAnalyticsListener()
    private val listeners = mutableSetOf<Listener>()

    override fun registerPlayer(player: ExoPlayer) {
        player.addAnalyticsListener(analyticsListener)
    }

    override fun unregisterPlayer(player: ExoPlayer) {
        player.removeAnalyticsListener(analyticsListener)
    }

    override fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private inline fun notifyListeners(event: Listener.() -> Unit) {
        listeners.toList()
            .forEach { listener ->
                listener.event()
            }
    }

    private inner class EventsDispatcherAnalyticsListener : PillarboxAnalyticsListener {
        override fun onPositionDiscontinuity(
            eventTime: EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @DiscontinuityReason reason: Int,
        ) {
            val oldItemIndex = oldPosition.mediaItemIndex
            val newItemIndex = newPosition.mediaItemIndex

            DebugLogger.debug(
                TAG,
                "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)} ($oldItemIndex -> $newItemIndex)"
            )

            if (oldItemIndex == newItemIndex && (reason == DISCONTINUITY_REASON_SEEK || reason == DISCONTINUITY_REASON_SEEK_ADJUSTMENT)) {
                val session = sessionManager.getCurrentSession() ?: return

                notifyListeners { onSeek(session) }
            }
        }

        override fun onPlayerError(eventTime: EventTime, error: PlaybackException) {
            val session = sessionManager.getSessionFromEventTime(eventTime) ?: return

            notifyListeners { onError(session) }
        }

        override fun onAudioPositionAdvancing(
            eventTime: EventTime,
            playoutStartSystemTimeMs: Long,
        ) {
            val session = sessionManager.getSessionFromEventTime(eventTime) ?: return

            notifyListeners { onMediaStart(session) }
        }

        override fun onRenderedFirstFrame(
            eventTime: EventTime,
            output: Any,
            renderTimeMs: Long,
        ) {
            val session = sessionManager.getSessionFromEventTime(eventTime) ?: return

            notifyListeners { onMediaStart(session) }
        }

        override fun onStallChanged(eventTime: EventTime, isStall: Boolean) {
            if (isStall) {
                val session = sessionManager.getSessionFromEventTime(eventTime) ?: return

                notifyListeners { onStall(session) }
            }
        }

        override fun onPlayerReleased(eventTime: EventTime) {
            DebugLogger.debug(TAG, "onPlayerReleased")
            notifyListeners { onPlayerReleased() }
        }

        override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
            val session = sessionManager.getSessionFromEventTime(eventTime) ?: return

            notifyListeners { onIsPlaying(session, isPlaying) }
        }
    }

    private companion object {
        private const val TAG = "PillarboxEventsDispatcher"
    }
}
