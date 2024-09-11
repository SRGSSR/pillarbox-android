/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.monitoring.models.ErrorMessageData
import ch.srgssr.pillarbox.player.monitoring.models.EventMessageData
import ch.srgssr.pillarbox.player.monitoring.models.EventMessageData.StreamType
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.monitoring.models.Message.EventName
import ch.srgssr.pillarbox.player.monitoring.models.MessageData
import ch.srgssr.pillarbox.player.monitoring.models.Session
import ch.srgssr.pillarbox.player.monitoring.models.Timings
import ch.srgssr.pillarbox.player.runOnApplicationLooper
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.Heartbeat
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class Monitoring(
    private val context: Context,
    private val player: ExoPlayer,
    private val metricsCollector: MetricsCollector,
    private val messageHandler: MonitoringMessageHandler,
    private val sessionManager: PlaybackSessionManager,
    private val coroutineContext: CoroutineContext,
) : PillarboxAnalyticsListener,
    MetricsCollector.Listener,
    PlaybackSessionManager.Listener {
    private val window = Window()

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
        var qoeTimings = Timings.QoE()
        var qosTimings = Timings.QoS()
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

    fun getCurrentQoETimings(): Timings.QoE? {
        val currentSession = sessionManager.getCurrentSession()

        return sessionHolders[currentSession?.sessionId]?.qoeTimings
    }

    fun getCurrentQoSTimings(): Timings.QoS? {
        val currentSession = sessionManager.getCurrentSession()

        return sessionHolders[currentSession?.sessionId]?.qosTimings
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        sessionHolders[session.sessionId] = SessionHolder(session, coroutineContext = coroutineContext) {
            player.runOnApplicationLooper {
                sendEvent(EventName.HEARTBEAT, session)
            }
        }
    }

    override fun onCurrentSessionChanged(
        oldSession: PlaybackSessionManager.SessionInfo?,
        newSession: PlaybackSessionManager.SessionInfo?,
    ) {
        oldSession?.let { sessionInfo ->
            metricsCollector.getMetricsForSession(sessionInfo.session)
        }?.let { metrics ->
            sessionHolders.remove(metrics.sessionId)?.let { holder ->
                if (holder.state == SessionHolder.State.STARTED) {
                    holder.state = SessionHolder.State.STOPPED
                    sendEvent(
                        eventName = EventName.STOP,
                        session = holder.session,
                        data = metrics.toQoSEvent(oldSession.position, oldSession.session.window),
                    )
                }
            } ?: Log.wtf(TAG, "Should have a session!")
        }

        val session = newSession?.session ?: return
        val metrics = metricsCollector.getMetricsForSession(session) ?: return

        sessionHolders[session.sessionId]?.qosTimings = Timings.QoS(
            asset = metrics.loadDuration.source?.inWholeMilliseconds,
            drm = metrics.loadDuration.drm?.inWholeMilliseconds,
            metadata = metrics.loadDuration.asset.takeIf { it != ZERO }?.inWholeMilliseconds,
        )
    }

    override fun onMetricSessionReady(metrics: PlaybackMetrics) {
        DebugLogger.info(TAG, "onMetricSessionReady $metrics")
        sessionHolders[metrics.sessionId]?.let { holder ->
            val loadDuration = metrics.loadDuration
            val assetLoadingTime = ((loadDuration.source ?: ZERO) - (holder.qosTimings.asset?.milliseconds ?: ZERO))
                .takeIf { it != ZERO }
            val metadataLoadingTime = ((loadDuration.asset ?: ZERO) - (holder.qosTimings.metadata?.milliseconds ?: ZERO))
                .takeIf { it != ZERO }

            holder.qoeTimings = Timings.QoE(
                asset = assetLoadingTime?.inWholeMilliseconds,
                metadata = metadataLoadingTime?.inWholeMilliseconds,
                total = loadDuration.timeToReady?.inWholeMilliseconds,
            )

            sendStartEvent(sessionHolder = holder)
            holder.state = SessionHolder.State.STARTED
        }
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
                eventName = EventName.ERROR,
                session = session,
                data = ErrorMessageData(
                    throwable = error,
                    player = player,
                    severity = ErrorMessageData.Severity.FATAL,
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
        eventName: EventName,
        session: PlaybackSessionManager.Session,
        data: MessageData? = null,
    ) {
        val dataToSend = data
            ?: metricsCollector.getMetricsForSession(session)?.toQoSEvent(
                player.currentPosition,
                player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
            )
            ?: return
        val message = Message(
            data = dataToSend,
            eventName = eventName,
            sessionId = session.sessionId,
        )
        messageHandler.sendEvent(message)
    }

    private fun PlaybackMetrics.toQoSEvent(position: Long, window: Window): EventMessageData {
        return EventMessageData(
            bandwidth = bandwidth,
            bitrate = indicatedBitrate,
            bufferDuration = player.totalBufferedDuration,
            duration = window.durationMs,
            playbackDuration = playbackDuration.inWholeMilliseconds,
            position = position,
            positionTimestamp = window.getPositionTimestamp(position),
            stall = EventMessageData.Stall(
                count = stallCount,
                duration = stallDuration.inWholeMilliseconds,
            ),
            streamType = if (window.isLive) StreamType.LIVE else StreamType.ON_DEMAND,
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
            eventName = EventName.START,
            session = sessionHolder.session,
            data = Session(
                context = context,
                media = Session.Media(
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

        private fun Window.getPositionTimestamp(position: Long): Long? {
            if (position == C.TIME_UNSET || windowStartTimeMs == C.TIME_UNSET) return null
            return windowStartTimeMs + position
        }
    }
}
