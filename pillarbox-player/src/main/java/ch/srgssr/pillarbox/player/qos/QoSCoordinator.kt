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
import ch.srgssr.pillarbox.player.analytics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.PlaybackStats
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.Heartbeat
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class QoSCoordinator(
    private val context: Context,
    private val player: ExoPlayer,
    private val eventsDispatcher: QoSEventsDispatcher,
    private val startupTimesTracker: StartupTimesTracker,
    private val metricsCollector: MetricsCollector,
    private val messageHandler: QoSMessageHandler,
    private val sessionManager: PlaybackSessionManager,
    coroutineContext: CoroutineContext,
) : PillarboxAnalyticsListener {
    private val heartbeat = Heartbeat(
        period = HEARTBEAT_PERIOD,
        coroutineContext = coroutineContext,
        task = {
            val session = currentSession ?: return@Heartbeat

            sendEvent("HEARTBEAT", session)
        },
    )

    private var url: String = ""
    private var currentSession: PlaybackSessionManager.Session? = null

    init {
        val eventsDispatcherListener = EventsDispatcherListener()

        eventsDispatcher.registerPlayer(player)
        eventsDispatcher.addListener(eventsDispatcherListener)
        eventsDispatcher.addListener(startupTimesTracker)

        sessionManager.registerPlayer(player)
        sessionManager.addListener(eventsDispatcherListener)
        sessionManager.addListener(startupTimesTracker)

        player.addAnalyticsListener(startupTimesTracker)
        player.addAnalyticsListener(metricsCollector)
        player.addAnalyticsListener(this)
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

    private fun sendEvent(
        eventName: String,
        session: PlaybackSessionManager.Session,
        data: Any? = null,
    ) {
        val message = QoSMessage(
            data = data ?: metricsCollector.getCurrentMetrics().toQoSEvent(),
            eventName = eventName,
            sessionId = session.sessionId,
        )

        messageHandler.sendEvent(message)
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

    private inner class EventsDispatcherListener : PlaybackSessionManager.Listener, QoSEventsDispatcher.Listener {

        override fun onCurrentSession(session: PlaybackSessionManager.Session) {
            currentSession = session
        }

        override fun onSessionFinished(session: PlaybackSessionManager.Session) {
            heartbeat.stop()
            sendEvent("END", session)
            currentSession = null
        }

        override fun onMediaStart(session: PlaybackSessionManager.Session) {
            val startupTimes = startupTimesTracker.consumeStartupTimes(session.sessionId) ?: return

            heartbeat.start(restart = false)

            sendStartEvent(session, startupTimes)
        }

        override fun onIsPlaying(
            session: PlaybackSessionManager.Session,
            isPlaying: Boolean,
        ) {
            if (isPlaying) {
                heartbeat.start(restart = false)
            } else {
                heartbeat.stop()
            }
        }

        override fun onSeek(session: PlaybackSessionManager.Session) {
            sendEvent("SEEK", session)
        }

        override fun onStall(session: PlaybackSessionManager.Session) {
            sendEvent("STALL", session)
        }

        override fun onError(session: PlaybackSessionManager.Session) {
            if (sessionManager.getSessionById(session.sessionId) == null) {
                sendStartEvent(session, QoSSessionTimings.Zero)
            }

            player.playerError?.let {
                sendEvent(
                    eventName = "ERROR",
                    session = session,
                    data = QoSError(
                        throwable = it,
                        playerPosition = player.currentPosition,
                        severity = QoSError.Severity.FATAL,
                    ),
                )
            }
        }

        override fun onPlayerReleased() {
            eventsDispatcher.unregisterPlayer(player)
            eventsDispatcher.removeListener(this)
            eventsDispatcher.removeListener(startupTimesTracker)

            sessionManager.unregisterPlayer(player)
            sessionManager.removeListener(this)
            sessionManager.removeListener(startupTimesTracker)

            player.removeAnalyticsListener(startupTimesTracker)
            player.removeAnalyticsListener(metricsCollector)
            player.removeAnalyticsListener(this@QoSCoordinator)
        }

        private fun sendStartEvent(
            session: PlaybackSessionManager.Session,
            timings: QoSSessionTimings,
        ) {
            sendEvent(
                eventName = "START",
                session = session,
                data = QoSSession(
                    context = context,
                    mediaId = session.mediaItem.mediaId,
                    mediaSource = session.mediaItem.localConfiguration?.uri.toString(),
                    timings = timings,
                ),
            )
        }
    }

    private companion object {
        private const val BITS = 8
        private val HEARTBEAT_PERIOD = 10.seconds
        private const val TAG = "QoSCoordinator"
    }
}
