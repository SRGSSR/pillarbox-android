/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackStatsMetrics
import ch.srgssr.pillarbox.player.utils.DebugLogger

internal class QoSCoordinator(
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val startupTimesTracker: StartupTimesTracker,
    private val playbackStatsMetrics: PlaybackStatsMetrics,
    private val messageHandler: QoSMessageHandler,
) : PillarboxAnalyticsListener {
    init {
        eventsDispatcher.registerPlayer(player)
        eventsDispatcher.addListener(EventsDispatcherListener())
        eventsDispatcher.addListener(startupTimesTracker)

        player.addAnalyticsListener(startupTimesTracker)
        player.addAnalyticsListener(playbackStatsMetrics)
        player.addAnalyticsListener(this)
    }

    override fun onStallChanged(eventTime: AnalyticsListener.EventTime, isStalls: Boolean) {
        messageHandler.sendEvent(Any())
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        DebugLogger.debug(TAG, "onEvents ${playbackStatsMetrics.getCurrentMetrics()}")
    }

    private inner class EventsDispatcherListener : QoSEventsDispatcher.Listener {
        override fun onMediaStart(session: QoSEventsDispatcher.Session) {
            val startupTimes = startupTimesTracker.consumeStartupTimes(session.sessionId) ?: return

            DebugLogger.debug(TAG, "[${session.mediaItem.mediaId}] onMediaStart $startupTimes")
        }

        override fun onPlayerReleased() {
            eventsDispatcher.unregisterPlayer(player)
            eventsDispatcher.removeListener(this)
            eventsDispatcher.removeListener(startupTimesTracker)

            player.removeAnalyticsListener(startupTimesTracker)
            player.removeAnalyticsListener(playbackStatsMetrics)
            player.removeAnalyticsListener(this@QoSCoordinator)
        }
    }

    private companion object {
        private const val TAG = "QoSCoordinator"
    }
}
