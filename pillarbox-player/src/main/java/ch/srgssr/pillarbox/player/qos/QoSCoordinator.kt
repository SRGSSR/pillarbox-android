/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
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
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class QoSCoordinator(
    private val context: Context,
    private val player: ExoPlayer,
    private val metricsCollector: MetricsCollector,
    private val messageHandler: QoSMessageHandler,
    private val sessionManager: PlaybackSessionManager,
    private val coroutineContext: CoroutineContext,
) : PillarboxAnalyticsListener,
    MetricsCollector.Listener,
    PlaybackSessionManager.Listener {

    internal class SessionHolder(
        val session: PlaybackSessionManager.Session,
        coroutineContext: CoroutineContext,
        val heartbeatTask: (PlaybackSessionManager.Session) -> Unit = {},
    ) {
        private val heartbeat = Heartbeat(
            period = HEARTBEAT_PERIOD,
            coroutineContext = coroutineContext,
            task = {
                heartbeatTask(session)
            },
        )
        var assetUrl: String? = null
        var error: PlaybackException? = null
            set(value) {
                if (value != null) state = State.STOPPED
                field = value
            }
        var state: State = State.CREATED
            set(value) {
                when (value) {
                    State.STARTED -> heartbeat.start(false)
                    else -> heartbeat.stop()
                }
                field = value
            }

        enum class State {
            CREATED,
            STARTED,
            STOPPED,
        }
    }

    private val sessionHolders = mutableMapOf<String, SessionHolder>()

    init {
        sessionManager.addListener(this)
        player.addAnalyticsListener(this)
        metricsCollector.addListener(this)
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        sessionHolders[session.sessionId] = SessionHolder(session, coroutineContext = coroutineContext) {
            player.runOnApplicationLooper {
                sendEvent(EVENT_HB, session)
            }
        }
    }

    override fun onMetricSessionReady(metrics: PlaybackMetrics) {
        DebugLogger.info(TAG, "onMetricSessionReady $metrics")
        sessionHolders[metrics.sessionId]?.let { holder ->
            sendStartEvent(
                sessionHolder = holder,
                metrics = metrics
            )
            holder.state = SessionHolder.State.STARTED
        }
    }

    override fun onMetricSessionFinished(metrics: PlaybackMetrics) {
        sessionHolders.remove(metrics.sessionId)?.let { holder ->
            if (holder.state == SessionHolder.State.STARTED) {
                holder.state = SessionHolder.State.STOPPED
                sendStopEvent(holder.session, metrics)
            }
        } ?: Log.wtf(TAG, "Should have a session!")
    }

    override fun onLoadStarted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        setAssetUrlForEventTime(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadCanceled(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        setAssetUrlForEventTime(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        setAssetUrlForEventTime(eventTime, loadEventInfo, mediaLoadData)
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        val session = sessionManager.getSessionFromEventTime(eventTime) ?: return
        sessionHolders[session.sessionId]?.let { holder ->
            val playbackMetrics = metricsCollector.getMetricsForSession(holder.session)
            // Send StartEvent if it was not sent previously.
            if (holder.state != SessionHolder.State.STARTED) {
                sendStartEvent(sessionHolder = holder, metrics = playbackMetrics)
            }
            holder.error = error
            sendErrorEvent(session = session, error = error, url = playbackMetrics?.url.toString())
        }
    }

    private fun setAssetUrlForEventTime(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        if (eventTime.timeline.isEmpty || mediaLoadData.dataType != C.DATA_TYPE_MEDIA || mediaLoadData.dataType != C.DATA_TYPE_MANIFEST) return
        val session = sessionManager.getSessionFromEventTime(eventTime) ?: return
        sessionHolders[session.sessionId]?.let { holder ->
            if (holder.assetUrl == null) holder.assetUrl = loadEventInfo.uri.toString()
        }
    }

    private fun sendStopEvent(session: PlaybackSessionManager.Session, playbackMetrics: PlaybackMetrics) {
        sendEvent(eventName = EVENT_STOP, session = session, data = playbackMetrics.toQoSEvent())
    }

    private fun sendErrorEvent(session: PlaybackSessionManager.Session, error: PlaybackException, url: String) {
        sendEvent(
            eventName = EVENT_ERROR,
            session = session,
            data = QoSError(
                throwable = error,
                playerPosition = player.currentPosition,
                severity = QoSError.Severity.FATAL,
                url = url
            ),
        )
    }

    private fun sendEvent(
        eventName: String,
        session: PlaybackSessionManager.Session,
        data: Any? = null,
    ) {
        val dataToSend = data ?: metricsCollector.getMetricsForSession(session)?.toQoSEvent() ?: return
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

    private fun sendStartEvent(
        sessionHolder: SessionHolder,
        metrics: PlaybackMetrics?,
    ) {
        sendEvent(
            eventName = EVENT_START,
            session = sessionHolder.session,
            data = QoSSession(
                context = context,
                media = QoSMedia(
                    assetUrl = sessionHolder.assetUrl ?: "",
                    id = sessionHolder.session.mediaItem.mediaId,
                    metadataUrl = sessionHolder.session.mediaItem.localConfiguration?.uri.toString(),
                    origin = context.packageName,
                ),
                timeMetrics = QoSSessionTimings(
                    asset = metrics?.loadDuration?.source,
                    drm = metrics?.loadDuration?.drm,
                    metadata = metrics?.loadDuration?.asset,
                    total = metrics?.loadDuration?.timeToReady,
                ),
            ),
        )
    }

    private companion object {
        private val HEARTBEAT_PERIOD = 30.seconds
        private const val TAG = "QoSCoordinator"
        private const val EVENT_START = "START"
        private const val EVENT_ERROR = "ERROR"
        private const val EVENT_STOP = "STOP"
        private const val EVENT_HB = "HEARTBEAT"
    }
}
