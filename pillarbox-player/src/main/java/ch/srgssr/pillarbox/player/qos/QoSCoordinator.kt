/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackStats
import ch.srgssr.pillarbox.player.analytics.PlaybackStatsMetrics
import ch.srgssr.pillarbox.player.utils.DebugLogger

private const val BITS = 8

internal class QoSCoordinator(
    private val context: Context,
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val startupTimesTracker: StartupTimesTracker,
    private val playbackStatsMetrics: PlaybackStatsMetrics,
    private val messageHandler: QoSMessageHandler,
) : PillarboxAnalyticsListener {

    private var url: String = ""
    private val sessions = mutableMapOf<String, QoSSession>()
    private var currentSession: QoSEventsDispatcher.Session? = null

    init {
        eventsDispatcher.registerPlayer(player)
        eventsDispatcher.addListener(EventsDispatcherListener())
        eventsDispatcher.addListener(startupTimesTracker)

        player.addAnalyticsListener(startupTimesTracker)
        player.addAnalyticsListener(playbackStatsMetrics)
        player.addAnalyticsListener(this)
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        DebugLogger.debug(TAG, "onEvents ${playbackStatsMetrics.getCurrentMetrics()}")
    }

    override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        url = loadEventInfo.uri.toString()
    }

    private fun PlaybackStats.toQoSEvent(): QoSEvent {
        val bitrateBytes = bitrate / BITS
        val bandwidthBytes = bandwidth / BITS
        return QoSEvent(
            bandwidth = bandwidthBytes,
            bitrate = bitrateBytes,
            bufferDuration = bufferDuration.inWholeMilliseconds,
            playbackDuration = playbackDuration.inWholeMilliseconds,
            playerPosition = player.currentPosition,
            stallCount = stallCount,
            stallDuration = stallDuration.inWholeSeconds,
            url = url,
        )
    }

    private inner class EventsDispatcherListener : QoSEventsDispatcher.Listener {

        override fun onCurrentSession(session: QoSEventsDispatcher.Session) {
            currentSession = session
        }

        override fun onSessionFinished(session: QoSEventsDispatcher.Session) {
            val metrics = playbackStatsMetrics.getCurrentMetrics()
            messageHandler.sendEvent(QoSMessage(sessionId = session.sessionId, eventName = "END", data = metrics.toQoSEvent()))
        }

        override fun onMediaStart(session: QoSEventsDispatcher.Session) {
            val startupTimes = startupTimesTracker.consumeStartupTimes(session.sessionId) ?: return
            val qosSession = QoSSession(
                context = context,
                timings = startupTimes,
                mediaId = session.mediaItem.mediaId,
                mediaSource = session.mediaItem.localConfiguration?.uri.toString()
            )

            messageHandler.sendEvent(QoSMessage(sessionId = session.sessionId, eventName = "START", data = qosSession))
        }

        override fun onSeek(session: QoSEventsDispatcher.Session) {
            val metrics = playbackStatsMetrics.getCurrentMetrics()
            messageHandler.sendEvent(QoSMessage(sessionId = session.sessionId, eventName = "SEEK", data = metrics.toQoSEvent()))
        }

        override fun onStall(session: QoSEventsDispatcher.Session) {
            val metrics = playbackStatsMetrics.getCurrentMetrics()
            messageHandler.sendEvent(QoSMessage(sessionId = session.sessionId, eventName = "STALL", data = metrics.toQoSEvent()))
        }

        override fun onError(session: QoSEventsDispatcher.Session) {
            if (!sessions.containsKey(session.sessionId)) {
                val timing = QoSSessionTimings.Zero
                val qosSession = QoSSession(
                    context = context,
                    timings = timing,
                    mediaId = session.mediaItem.mediaId,
                    mediaSource = session.mediaItem.localConfiguration?.uri.toString()
                )
                messageHandler.sendEvent(QoSMessage(sessionId = session.sessionId, eventName = "START", data = qosSession))
            }
            player.playerError?.let {
                messageHandler.sendEvent(
                    QoSMessage(
                        sessionId = session.sessionId, eventName = "ERROR",
                        data = QoSError(
                            throwable = it,
                            severity = QoSError.Severity.FATAL,
                            playerPosition = player.currentPosition
                        )
                    )
                )
            }
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
