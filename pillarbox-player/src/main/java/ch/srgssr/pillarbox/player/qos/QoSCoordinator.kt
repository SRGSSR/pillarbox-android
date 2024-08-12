/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.qos.models.QoSError
import ch.srgssr.pillarbox.player.qos.models.QoSEvent
import ch.srgssr.pillarbox.player.qos.models.QoSMedia
import ch.srgssr.pillarbox.player.qos.models.QoSMessage
import ch.srgssr.pillarbox.player.qos.models.QoSSession
import ch.srgssr.pillarbox.player.qos.models.QoSSessionTimings
import ch.srgssr.pillarbox.player.qos.models.QoSStall
import ch.srgssr.pillarbox.player.runOnApplicationLooper
import ch.srgssr.pillarbox.player.utils.BitrateUtil.toByteRate
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.Heartbeat
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class QoSCoordinator(
    private val context: Context,
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val metricsCollector: MetricsCollector,
    private val messageHandler: QoSMessageHandler,
    private val sessionManager: PlaybackSessionManager,
    coroutineContext: CoroutineContext,
) : PillarboxAnalyticsListener, MetricsCollector.Listener {
    private val heartbeat = Heartbeat(
        period = HEARTBEAT_PERIOD,
        coroutineContext = coroutineContext,
        task = {
            val session = currentSession ?: return@Heartbeat
            player.runOnApplicationLooper {
                sendEvent("HEARTBEAT", session)
            }
        },
    )

    private var url: String = ""
    private var currentSession: PlaybackSessionManager.Session? = null

    init {
        val eventsDispatcherListener = EventsDispatcherListener()

        eventsDispatcher.registerPlayer(player)
        eventsDispatcher.addListener(eventsDispatcherListener)

        sessionManager.addListener(eventsDispatcherListener)
        player.addAnalyticsListener(this)
        metricsCollector.addListener(this)
    }

    override fun onMetricSessionReady(metrics: PlaybackMetrics) {
        DebugLogger.info(TAG, "onMetricSessionReady $metrics")

        heartbeat.start(restart = false)
        sessionManager.getSessionById(metrics.sessionId)?.let {
            sendStartEvent(
                session = it,
                timeMetrics = QoSSessionTimings(
                    asset = metrics.loadDuration.source,
                    drm = metrics.loadDuration.drm,
                    metadata = metrics.loadDuration.asset,
                    total = metrics.loadDuration.timeToReady,
                )
            )
        }
    }

    override fun onMetricSessionFinished(metrics: PlaybackMetrics) {
        heartbeat.stop()
        sessionManager.getSessionById(metrics.sessionId)?.let {
            sendEndEvent(it, metrics)
        } ?: Log.wtf(TAG, "Should have a session!")
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        DebugLogger.debug(TAG, "onEvents ${metricsCollector.getCurrentMetrics()}")
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        // TODO Check if this is linked to the current session before updating the URL
        url = loadEventInfo.uri.toString()
    }

    private fun sendEndEvent(session: PlaybackSessionManager.Session, playbackMetrics: PlaybackMetrics) {
        val dataToSend = playbackMetrics.toQoSEvent()
        val message = QoSMessage(
            data = dataToSend,
            eventName = "END",
            sessionId = session.sessionId,
        )
        messageHandler.sendEvent(message)
    }

    private fun sendEvent(
        eventName: String,
        session: PlaybackSessionManager.Session,
        data: Any? = null,
    ) {
        val dataToSend = data ?: metricsCollector.getCurrentMetrics()?.toQoSEvent() ?: return
        val message = QoSMessage(
            data = dataToSend,
            eventName = eventName,
            sessionId = session.sessionId,
        )
        messageHandler.sendEvent(message)
    }

    private fun PlaybackMetrics.toQoSEvent(): QoSEvent {
        val bitrateBytes = indicatedBitrate.toByteRate()
        val bandwidthBytes = bandwidth.toByteRate()
        return QoSEvent(
            bandwidth = bandwidthBytes,
            bitrate = bitrateBytes.toInt(),
            bufferDuration = player.totalBufferedDuration,
            playbackDuration = playbackDuration.inWholeMilliseconds,
            playerPosition = player.currentPosition,
            stall = QoSStall(
                count = stallCount,
                duration = stallDuration.inWholeMilliseconds,
            ),
            url = url.toString(),
        )
    }

    private inner class EventsDispatcherListener : PlaybackSessionManager.Listener, QoSEventsDispatcher.Listener {

        override fun onCurrentSession(session: PlaybackSessionManager.Session) {
            currentSession = session
        }

        override fun onSessionFinished(session: PlaybackSessionManager.Session) {
            currentSession = null
        }

        override fun onMediaStart(session: PlaybackSessionManager.Session) {
        }

        override fun onSeek(session: PlaybackSessionManager.Session) {
            sendEvent("SEEK", session)
        }

        override fun onStall(session: PlaybackSessionManager.Session) {
            sendEvent("STALL", session)
        }

        override fun onError(session: PlaybackSessionManager.Session) {
            if (sessionManager.getSessionById(session.sessionId) == null) {
                sendStartEvent(session, QoSSessionTimings.Empty)
            }

            player.playerError?.let {
                sendEvent(
                    eventName = "ERROR",
                    session = session,
                    data = QoSError(
                        throwable = it,
                        playerPosition = player.currentPosition,
                        severity = QoSError.Severity.FATAL,
                        url = url,
                    ),
                )
            }
        }

        override fun onPlayerReleased() {
            eventsDispatcher.unregisterPlayer(player)
            eventsDispatcher.removeListener(this)
            sessionManager.removeListener(this)
        }
    }

    private fun sendStartEvent(
        session: PlaybackSessionManager.Session,
        timeMetrics: QoSSessionTimings,
    ) {
        sendEvent(
            eventName = "START",
            session = session,
            data = QoSSession(
                context = context,
                media = QoSMedia(
                    assetUrl = url,
                    id = session.mediaItem.mediaId,
                    metadataUrl = session.mediaItem.localConfiguration?.uri.toString(),
                    origin = context.packageName,
                ),
                timeMetrics = timeMetrics,
            ),
        )
    }

    private companion object {
        private val HEARTBEAT_PERIOD = 30.seconds
        private const val TAG = "QoSCoordinator"
    }
}