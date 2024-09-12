/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import androidx.annotation.VisibleForTesting
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.util.Size
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.utils.DebugLogger
import java.io.IOException

/**
 * Playback stats metrics
 * Compute playback stats metrics likes stalls, playtime, bitrate, etc...
 */
class MetricsCollector @VisibleForTesting private constructor(
    private val timeProvider: () -> Long,
) : PillarboxAnalyticsListener, PlaybackSessionManager.Listener {
    /**
     * Listener
     */
    interface Listener {
        /**
         * On metric session ready
         *
         * @param metrics
         */
        fun onMetricSessionReady(metrics: PlaybackMetrics) = Unit
    }

    private val window = Window()
    private var currentSession: PlaybackSessionManager.Session? = null
    private val listeners = mutableSetOf<Listener>()
    private lateinit var player: PillarboxExoPlayer
    private val metricsSessions = mutableMapOf<Any, SessionMetrics>()
    private var surfaceSize: Size = Size.UNKNOWN

    constructor() : this({ System.currentTimeMillis() })

    /**
     * Set player at [PillarboxExoPlayer] creation.
     */
    fun setPlayer(player: PillarboxExoPlayer) {
        player.sessionManager.addListener(this)
        player.addAnalyticsListener(this)
        this.player = player
    }

    /**
     * Add listener
     *
     * @param listener
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /**
     * Remove listener
     *
     * @param listener
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

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
                setIsPlaying(player.isPlaying)
                setPlaybackState(player.playbackState)
            }
        }
    }

    override fun onSessionDestroyed(session: PlaybackSessionManager.Session) {
        DebugLogger.debug(TAG, "onSessionDestroyed ${session.sessionId}")
        metricsSessions.remove(session.periodUid)
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

    private fun getOrCreateSessionMetrics(periodUid: Any): SessionMetrics {
        return metricsSessions.getOrPut(periodUid) {
            SessionMetrics(timeProvider) { sessionMetrics ->
                player.sessionManager.getSessionFromPeriodUid(periodUid)?.let {
                    notifyMetricsReady(createPlaybackMetrics(session = it, metrics = sessionMetrics))
                }
            }
        }
    }

    override fun onStallChanged(eventTime: EventTime, isStall: Boolean) {
        getSessionMetrics(eventTime)?.setIsStall(isStall)
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
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
        if (player.playbackState == Player.STATE_IDLE || eventTime.timeline.isEmpty) return
        getSessionMetrics(eventTime)?.videoFormat = null
    }

    override fun onAudioInputFormatChanged(eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        getSessionMetrics(eventTime)?.audioFormat = format
    }

    override fun onAudioDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        if (player.playbackState == Player.STATE_IDLE || eventTime.timeline.isEmpty) return
        getSessionMetrics(eventTime)?.audioFormat = null
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
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

    override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
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

    override fun onSurfaceSizeChanged(eventTime: EventTime, width: Int, height: Int) {
        surfaceSize = Size(width, height)
    }

    /**
     * Get current metrics
     *
     * @return metrics to the current time
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
        )
    }

    /**
     * Get metrics for session
     *
     * @param session
     * @return
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
