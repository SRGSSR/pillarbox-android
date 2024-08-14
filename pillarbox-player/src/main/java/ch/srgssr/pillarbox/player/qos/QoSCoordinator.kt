/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
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
    coroutineContext: CoroutineContext,
) : PillarboxAnalyticsListener,
    MetricsCollector.Listener,
    PlaybackSessionManager.Listener {
    private val heartbeat = Heartbeat(
        period = HEARTBEAT_PERIOD,
        coroutineContext = coroutineContext,
        task = {
            val session = currentSession ?: return@Heartbeat
            player.runOnApplicationLooper {
                sendEvent(EVENT_HB, session)
            }
        },
    )

    private var currentSession: PlaybackSessionManager.Session? = null
    private val assetUrls = mutableMapOf<Any, String>()

    init {
        sessionManager.addListener(this)
        player.addAnalyticsListener(this)
        metricsCollector.addListener(this)
    }

    override fun onCurrentSession(session: PlaybackSessionManager.Session) {
        this.currentSession = session
    }

    override fun onSessionFinished(session: PlaybackSessionManager.Session) {
        currentSession = null
        heartbeat.stop()
    }

    override fun onMetricSessionReady(metrics: PlaybackMetrics) {
        DebugLogger.info(TAG, "onMetricSessionReady $metrics")

        heartbeat.start(restart = false)
        sessionManager.getSessionById(metrics.sessionId)?.let {
            sendStartEvent(
                session = it,
                metrics = metrics
            )
        }
    }

    override fun onMetricSessionFinished(metrics: PlaybackMetrics) {
        heartbeat.stop()
        sessionManager.getSessionById(metrics.sessionId)?.let {
            sendEndEvent(it, metrics)
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
        val session = sessionManager.getSessionFromEventTime(eventTime)
        session?.let {
            val playbackMetrics = metricsCollector.getMetricsForSession(it)
            sendStartEvent(session = it, metrics = playbackMetrics)
            sendErrorEvent(session = it, error = error, url = playbackMetrics?.url.toString())
        }
    }

    private fun setAssetUrlForEventTime(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        if (eventTime.timeline.isEmpty) return
        val periodUid = eventTime.getUidOfPeriod(Timeline.Window())
        if (!assetUrls.containsKey(periodUid) && (mediaLoadData.dataType == C.DATA_TYPE_MEDIA || mediaLoadData.dataType == C.DATA_TYPE_MANIFEST)) {
            assetUrls[periodUid] = loadEventInfo.uri.toString()
        }
    }

    private fun sendEndEvent(session: PlaybackSessionManager.Session, playbackMetrics: PlaybackMetrics) {
        sendEvent(eventName = EVENT_END, session = session, data = playbackMetrics.toQoSEvent())
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

    private fun sendStartEvent(
        session: PlaybackSessionManager.Session,
        metrics: PlaybackMetrics?,
    ) {
        sendEvent(
            eventName = EVENT_START,
            session = session,
            data = QoSSession(
                context = context,
                media = QoSMedia(
                    assetUrl = assetUrls[session.periodUid] ?: "",
                    id = session.mediaItem.mediaId,
                    metadataUrl = session.mediaItem.localConfiguration?.uri.toString(),
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
        const val EVENT_START = "START"
        const val EVENT_ERROR = "ERROR"
        const val EVENT_END = "END"
        const val EVENT_HB = "HEARTBEAT"
    }
}
