/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.extension.getPositionTimestamp
import ch.srgssr.pillarbox.player.qos.models.QoETimings
import ch.srgssr.pillarbox.player.qos.models.QoSError
import ch.srgssr.pillarbox.player.qos.models.QoSEvent
import ch.srgssr.pillarbox.player.qos.models.QoSEvent.StreamType
import ch.srgssr.pillarbox.player.qos.models.QoSMedia
import ch.srgssr.pillarbox.player.qos.models.QoSMessage
import ch.srgssr.pillarbox.player.qos.models.QoSSession
import ch.srgssr.pillarbox.player.qos.models.QoSStall
import ch.srgssr.pillarbox.player.qos.models.QoSTimings
import ch.srgssr.pillarbox.player.runOnApplicationLooper
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.Heartbeat
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.ZERO
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
    private val window = Timeline.Window()

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
        var qoeTimings = QoETimings()
        var qosTimings = QoSTimings()
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

    fun getCurrentQoETimings(): QoETimings? {
        val currentSession = sessionManager.getCurrentSession()

        return sessionHolders[currentSession?.sessionId]?.qoeTimings
    }

    fun getCurrentQoSTimings(): QoSTimings? {
        val currentSession = sessionManager.getCurrentSession()

        return sessionHolders[currentSession?.sessionId]?.qosTimings
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        sessionHolders[session.sessionId] = SessionHolder(session, coroutineContext = coroutineContext) {
            player.runOnApplicationLooper {
                sendEvent(EVENT_HB, session)
            }
        }
    }

    override fun onCurrentSessionChanged(
        oldSession: PlaybackSessionManager.SessionInfo?,
        newSession: PlaybackSessionManager.SessionInfo?,
    ) {
        val session = newSession?.session ?: return
        val metrics = metricsCollector.getMetricsForSession(session) ?: return

        sessionHolders[session.sessionId]?.qosTimings = QoSTimings(
            asset = metrics.loadDuration.source,
            drm = metrics.loadDuration.drm,
            metadata = metrics.loadDuration.asset.takeIf { it != ZERO },
        )
    }

    override fun onMetricSessionReady(metrics: PlaybackMetrics) {
        DebugLogger.info(TAG, "onMetricSessionReady $metrics")
        sessionHolders[metrics.sessionId]?.let { holder ->
            val loadDuration = metrics.loadDuration
            val assetLoadingTime = ((loadDuration.source ?: ZERO) - (holder.qosTimings.asset ?: ZERO)).takeIf { it != ZERO }
            val metadataLoadingTime = ((loadDuration.asset ?: ZERO) - (holder.qosTimings.metadata ?: ZERO)).takeIf { it != ZERO }

            holder.qoeTimings = QoETimings(
                asset = assetLoadingTime,
                metadata = metadataLoadingTime,
                total = loadDuration.timeToReady,
            )

            sendStartEvent(sessionHolder = holder)
            holder.state = SessionHolder.State.STARTED
        }
    }

    override fun onMetricSessionFinished(metrics: PlaybackMetrics, position: Long, positionTimestamp: Long?) {
        sessionHolders.remove(metrics.sessionId)?.let { holder ->
            if (holder.state == SessionHolder.State.STARTED) {
                holder.state = SessionHolder.State.STOPPED
                sendEvent(
                    eventName = EVENT_STOP,
                    session = holder.session,
                    data = metrics.toQoSEvent(position, positionTimestamp),
                )
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
                sendStartEvent(sessionHolder = holder)
            }
            holder.error = error

            sendEvent(
                eventName = EVENT_ERROR,
                session = session,
                data = QoSError(
                    throwable = error,
                    player = player,
                    severity = QoSError.Severity.FATAL,
                    url = playbackMetrics?.url.toString(),
                ),
            )
        }
    }

    private fun setAssetUrlForEventTime(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        if (eventTime.timeline.isEmpty || mediaLoadData.dataType != C.DATA_TYPE_MEDIA || mediaLoadData.dataType != C.DATA_TYPE_MANIFEST) return
        val session = sessionManager.getSessionFromEventTime(eventTime) ?: return
        sessionHolders[session.sessionId]?.let { holder ->
            if (holder.assetUrl == null) holder.assetUrl = loadEventInfo.uri.toString()
        }
    }

    private fun sendEvent(
        eventName: String,
        session: PlaybackSessionManager.Session,
        data: Any? = null,
    ) {
        val dataToSend = data
            ?: metricsCollector.getMetricsForSession(session)?.toQoSEvent(player.currentPosition, player.getPositionTimestamp(window))
            ?: return
        val message = QoSMessage(
            data = dataToSend,
            eventName = eventName,
            sessionId = session.sessionId,
        )
        messageHandler.sendEvent(message)
    }

    private fun PlaybackMetrics.toQoSEvent(position: Long, positionTimestamp: Long?): QoSEvent {
        return QoSEvent(
            bandwidth = bandwidth,
            bitrate = indicatedBitrate,
            bufferDuration = player.totalBufferedDuration,
            duration = player.duration,
            playbackDuration = playbackDuration.inWholeMilliseconds,
            position = position,
            positionTimestamp = positionTimestamp,
            stall = QoSStall(
                count = stallCount,
                duration = stallDuration.inWholeMilliseconds,
            ),
            streamType = if (player.isCurrentMediaItemLive) StreamType.LIVE else StreamType.ON_DEMAND,
            url = url.toString(),
            vpn = hasActiveVPN(),
        )
    }

    private fun hasActiveVPN(): Boolean? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            null
        }
    }

    private fun sendStartEvent(sessionHolder: SessionHolder) {
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
                qoeTimings = sessionHolder.qoeTimings,
                qosTimings = sessionHolder.qosTimings,
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
