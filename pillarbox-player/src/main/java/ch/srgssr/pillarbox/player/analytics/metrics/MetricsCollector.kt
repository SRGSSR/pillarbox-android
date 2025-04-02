/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Size
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.utils.DebugLogger
import java.io.IOException

/**
 * This class is responsible for collecting and computing playback statistics metrics such as:
 * - Stalls (count and duration)
 * - Playtime
 * - Bitrate (estimated and indicated)
 * - Loading times (manifest, asset, source, DRM)
 * - Video and audio format information
 * - Dropped video frames
 * - Surface size
 */
class MetricsCollector(
    private val sessionManager: PlaybackSessionManager,
    private val clock: Clock,
) : PillarboxAnalyticsListener {

    /**
     * A listener interface for receiving updates about playback metrics.
     */
    interface Listener {
        /**
         * Invoked when the player has collected enough information to start reporting playback metrics.
         *
         * @param metrics The [PlaybackMetrics] object containing various playback metrics.
         */
        fun onMetricSessionReady(metrics: PlaybackMetrics) = Unit
    }

    private var currentSession: PlaybackSessionManager.Session? = null
    private val listeners = mutableSetOf<Listener>()
    private val metricsSessions = mutableMapOf<Any, SessionMetrics>()
    private var surfaceSize: Size = Size.UNKNOWN
    private val window = Window()
    private var playbackState: Int = Player.STATE_IDLE
    private var isPlaying: Boolean = false

    init {
        sessionManager.addListener(MetricsSessionManagerListener())
    }

    /**
     * Registers a listener to receive events.
     *
     * @param listener The listener to be added.
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /**
     * Removes the specified listener from the list of listeners.
     *
     * @param listener The listener to be removed.
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private inner class MetricsSessionManagerListener : PlaybackSessionManager.Listener {
        private fun notifyMetricsReady(playbackMetrics: PlaybackMetrics) {
            if (currentSession?.sessionId != playbackMetrics.sessionId) return
            DebugLogger.debug(TAG, "notifyMetricsReady $playbackMetrics")
            listeners.toList().forEach {
                it.onMetricSessionReady(metrics = playbackMetrics)
            }
        }

        override fun onSessionCreated(session: PlaybackSessionManager.Session) {
            DebugLogger.debug(TAG, "onSessionCreated ${session.sessionId}")
            getOrCreateSessionMetrics(session.periodUid)
        }

        override fun onCurrentSessionChanged(oldSession: PlaybackSessionManager.SessionInfo?, newSession: PlaybackSessionManager.SessionInfo?) {
            DebugLogger.debug(TAG, "onCurrentSession ${oldSession?.session?.sessionId} -> ${newSession?.session?.sessionId}")
            currentSession = newSession?.session
            currentSession?.let { session ->
                getOrCreateSessionMetrics(session.periodUid).apply {
                    setIsPlaying(isPlaying)
                    setPlaybackState(playbackState)
                }
            }
        }

        override fun onSessionDestroyed(session: PlaybackSessionManager.Session) {
            DebugLogger.debug(TAG, "onSessionDestroyed ${session.sessionId}")
            metricsSessions.remove(session.periodUid)
        }

        private fun getOrCreateSessionMetrics(periodUid: Any): SessionMetrics {
            return metricsSessions.getOrPut(periodUid) {
                SessionMetrics(clock::currentTimeMillis) { sessionMetrics ->
                    sessionManager.getSessionFromPeriodUid(periodUid)?.let {
                        notifyMetricsReady(createPlaybackMetrics(session = it, metrics = sessionMetrics))
                    }
                }
            }
        }
    }

    override fun onStallChanged(eventTime: EventTime, isStall: Boolean) {
        getSessionMetrics(eventTime)?.setIsStall(isStall)
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        this.isPlaying = isPlaying
        getSessionMetrics(eventTime)?.setIsPlaying(isPlaying)
    }

    override fun onBandwidthEstimate(eventTime: EventTime, totalLoadTimeMs: Int, totalBytesLoaded: Long, bitrateEstimate: Long) {
        getSessionMetrics(eventTime)?.setBandwidthEstimate(totalLoadTimeMs, totalBytesLoaded, bitrateEstimate)
    }

    override fun onVideoInputFormatChanged(eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        getSessionMetrics(eventTime)?.videoFormat = format
    }

    /**
     * On video disabled is called when releasing the player
     *
     * @param eventTime
     * @param decoderCounters
     */
    override fun onVideoDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        if (playbackState == Player.STATE_IDLE || eventTime.timeline.isEmpty) return
        getSessionMetrics(eventTime)?.videoFormat = null
    }

    override fun onAudioInputFormatChanged(eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        getSessionMetrics(eventTime)?.audioFormat = format
    }

    override fun onAudioDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        if (playbackState == Player.STATE_IDLE || eventTime.timeline.isEmpty) return
        getSessionMetrics(eventTime)?.audioFormat = null
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        playbackState = state
        getSessionMetrics(eventTime)?.setPlaybackState(state)
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        getSessionMetrics(eventTime)?.setRenderFirstFrameOrAudioPositionAdvancing()
    }

    override fun onAudioPositionAdvancing(eventTime: EventTime, playoutStartSystemTimeMs: Long) {
        getSessionMetrics(eventTime)?.setRenderFirstFrameOrAudioPositionAdvancing()
    }

    override fun onLoadCompleted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        getSessionMetrics(eventTime)?.setLoadCompleted(loadEventInfo, mediaLoadData)
    }

    override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData, retryCount: Int) {
        getSessionMetrics(eventTime)?.setLoadStarted(loadEventInfo)
    }

    override fun onLoadError(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        getSessionMetrics(eventTime)?.setLoadCompleted(loadEventInfo, mediaLoadData)
    }

    override fun onDrmSessionAcquired(eventTime: EventTime, state: Int) {
        DebugLogger.debug(TAG, "onDrmSessionAcquired $state")
        if (state == DrmSession.STATE_OPENED) {
            getSessionMetrics(eventTime)?.setDrmSessionAcquired()
        }
    }

    override fun onDrmSessionReleased(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onDrmSessionReleased")
    }

    override fun onDrmKeysLoaded(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onDrmKeysLoaded")
        getSessionMetrics(eventTime)?.setDrmKeyLoaded()
    }

    override fun onDrmKeysRestored(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onDrmKeysRestored")
        getSessionMetrics(eventTime)?.setDrmKeyLoaded()
    }

    override fun onDrmKeysRemoved(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onDrmKeysRemoved")
        getSessionMetrics(eventTime)?.setDrmKeyLoaded()
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        listeners.clear()
    }

    override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
        getSessionMetrics(eventTime)?.let {
            it.totalDroppedFrames += droppedFrames
        }
    }

    override fun onSurfaceSizeChanged(eventTime: EventTime, width: Int, height: Int) {
        surfaceSize = Size(width, height)
    }

    /**
     * Get session metrics
     *
     * @param eventTime
     * @return `null` if there is no item in the timeline or session already finished.
     */
    private fun getSessionMetrics(eventTime: EventTime): SessionMetrics? {
        if (eventTime.timeline.isEmpty) return null
        return metricsSessions[(eventTime.getUidOfPeriod(window))]
    }

    /**
     * Retrieves the current playback metrics.
     *
     * @return The playback metrics for the current session, or `null` if there is no active session.
     */
    fun getCurrentMetrics(): PlaybackMetrics? {
        return currentSession?.let {
            getMetricsForSession(it)
        }
    }

    private fun createPlaybackMetrics(session: PlaybackSessionManager.Session, metrics: SessionMetrics): PlaybackMetrics {
        return PlaybackMetrics(
            sessionId = session.sessionId,
            bandwidth = metrics.estimateBitrate,
            indicatedBitrate = metrics.getTotalBitrate(),
            playbackDuration = metrics.totalPlayingDuration,
            stallCount = metrics.stallCount,
            stallDuration = metrics.totalStallDuration,
            bufferingDuration = metrics.totalBufferingDuration,
            loadDuration = PlaybackMetrics.LoadDuration(
                drm = metrics.totalDrmLoadingDuration,
                asset = metrics.asset,
                source = metrics.source,
                manifest = metrics.manifest,
                timeToReady = metrics.timeToReady,
            ),
            videoFormat = metrics.videoFormat,
            audioFormat = metrics.audioFormat,
            totalLoadTime = metrics.totalLoadTime,
            totalBytesLoaded = metrics.totalBytesLoaded,
            url = metrics.url,
            surfaceSize = surfaceSize,
            totalDroppedFrames = metrics.totalDroppedFrames,
        )
    }

    /**
     * Retrieves playback metrics for a given playback session.
     *
     * @param session The playback session for which to retrieve metrics.
     * @return A [PlaybackMetrics] containing the session's metrics, or `null` if no metrics are found for the session.
     */
    fun getMetricsForSession(session: PlaybackSessionManager.Session): PlaybackMetrics? {
        return metricsSessions[session.periodUid]?.let {
            createPlaybackMetrics(session, it)
        }
    }

    private companion object {
        private const val TAG = "MetricsCollector"
    }
}
