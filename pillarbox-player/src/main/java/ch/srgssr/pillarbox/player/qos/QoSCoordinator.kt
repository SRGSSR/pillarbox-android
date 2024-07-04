/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackStatsMetrics

internal class QoSCoordinator(
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val qoSSessionAnalyticsListener: QoSSessionAnalyticsListener,
    private val playbackStatsMetrics: PlaybackStatsMetrics,
    private val messageHandler: QoSMessageHandler,
) : PillarboxAnalyticsListener {
    init {
        eventsDispatcher.registerPlayer(player)
        eventsDispatcher.addListener(EventsDispatcherListener())
    }

    init {
        player.addAnalyticsListener(playbackStatsMetrics)
        player.addAnalyticsListener(this)
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        player.removeAnalyticsListener(playbackStatsMetrics)
        player.removeAnalyticsListener(this)
    }

    override fun onStallChanged(eventTime: AnalyticsListener.EventTime, isStalls: Boolean) {
        messageHandler.sendEvent(Any())
    }

    private inner class EventsDispatcherListener : QoSEventsDispatcher.Listener {
        override fun onSessionCreated(session: QoSEventsDispatcher.Session) {
            qoSSessionAnalyticsListener.onSessionCreated(session)
        }

        override fun onCurrentSession(session: QoSEventsDispatcher.Session) {
            qoSSessionAnalyticsListener.onCurrentSession(session)
        }

        override fun onMediaStart(session: QoSEventsDispatcher.Session) {
            // TODO
        }

        override fun onSeek(session: QoSEventsDispatcher.Session) {
            // TODO
        }

        override fun onStall(session: QoSEventsDispatcher.Session) {
            // TODO
        }

        override fun onSessionFinished(session: QoSEventsDispatcher.Session) {
            qoSSessionAnalyticsListener.onSessionFinished(session)
        }

        override fun onPlayerReleased() {
            eventsDispatcher.unregisterPlayer(player)
            eventsDispatcher.removeListener(this)
        }
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        Log.d(TAG, "onEvents ${playbackStatsMetrics.getCurrentMetrics()}")
    }

    companion object {
        private const val TAG = "QoSCoordinator"
    }
}
