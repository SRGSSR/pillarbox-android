/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.PlaybackStatsMetrics

internal class QoSCoordinator(
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val qoSSessionAnalyticsListener: QoSSessionAnalyticsListener,
    val playbackStatsMetrics: PlaybackStatsMetrics,
    val messageHandler: QoSMessageHandler,
) : PillarboxAnalyticsListener, PlaybackSessionManager.Listener {

    override fun onStallChanged(eventTime: AnalyticsListener.EventTime, isStalls: Boolean) {
        messageHandler.sendEvent(Any())
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        qoSSessionAnalyticsListener.onSessionCreated(session)
    }

    override fun onSessionFinished(session: PlaybackSessionManager.Session) {
        qoSSessionAnalyticsListener.onSessionFinished(session)
    }

    override fun onCurrentSession(session: PlaybackSessionManager.Session) {
        qoSSessionAnalyticsListener.onCurrentSession(session)
    }
}
