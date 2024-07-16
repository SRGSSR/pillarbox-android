/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.TotalPlaytimeCounter
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
         * On metric session finished
         *
         * @param metrics The [PlaybackMetrics] that belong to te finished session.
         */
        fun onMetricSessionFinished(metrics: PlaybackMetrics) = Unit

        /**
         * On metric session ready
         *
         * @param metrics
         */
        fun onMetricSessionReady(metrics: PlaybackMetrics) = Unit
    }

    private val totalPlaytimeCounter: TotalPlaytimeCounter = TotalPlaytimeCounter(timeProvider)
    private val totalStallTimeCounter: TotalPlaytimeCounter = TotalPlaytimeCounter(timeProvider)
    private val totalBufferingTimeCounter: TotalPlaytimeCounter = TotalPlaytimeCounter(timeProvider)
    private var stallCount = 0
    private var bandwidth = 0L
    private var bufferDuration = Duration.ZERO
    private var audioFormat: Format? = null
    private var videoFormat: Format? = null
    private val window = Window()
    private val loadingTimes = mutableMapOf<Any, LoadingTimes>()
    private var currentSession: PlaybackSessionManager.Session? = null
    private val listeners = mutableSetOf<Listener>()
    private lateinit var player: PillarboxExoPlayer

    constructor() : this({ System.currentTimeMillis() })

    /**
     * Set player at [PillarboxExoPlayer] creation.
     */
    fun setPlayer(player: PillarboxExoPlayer) {
        println("[setPlayer] $player")

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

    private fun notifyMetricsFinished(metrics: PlaybackMetrics) {
        println("[notifyMetricsFinished] ${metrics.sessionId}")

        listeners.toList().forEach {
            it.onMetricSessionFinished(metrics)
        }
    }

    private fun notifyMetricsReady(metrics: PlaybackMetrics) {
        if (currentSession?.sessionId != metrics.sessionId) {
            println("[notifyMetricsReady][${metrics.sessionId}] Session is NOT current")

            return
        }

        println("[notifyMetricsReady][${metrics.sessionId}] Session is current")

        DebugLogger.debug(TAG, "notifyMetricsReady $metrics")
        listeners.toList().forEach {
            it.onMetricSessionReady(metrics)
        }
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        println("[onSessionCreated] ${session.sessionId}")

        getOrCreateLoadingTimes(session.periodUid)
    }

    override fun onSessionFinished(session: PlaybackSessionManager.Session) {
        println("[onSessionFinished] ${session.sessionId}")

        getMetricsForSession(session)?.let {
            println("[onSessionFinished] notifyMetricsFinished")

            DebugLogger.debug(TAG, "onSessionFinished: $it")
            notifyMetricsFinished(it)
        }

        println("[onSessionFinished] clear state")

        loadingTimes.remove(session.periodUid)
        reset()
    }

    override fun onCurrentSession(session: PlaybackSessionManager.Session) {
        println("[onCurrentSession] ${session.sessionId}")

        currentSession = session
        val loadingTimes = loadingTimes[session.periodUid]
        if (loadingTimes?.state == Player.STATE_READY) {
            println("[onCurrentSession] loading times ready")

            getCurrentMetrics()?.let(this::notifyMetricsReady)
        }
    }

    private fun getOrCreateLoadingTimes(periodUid: Any): LoadingTimes {
        println("[getOrCreateLoadingTimes] $periodUid")

        return loadingTimes.getOrPut(periodUid) {
            println("[getOrCreateLoadingTimes] create new LoadingTimes")

            LoadingTimes(timeProvider = timeProvider, onLoadingReady = {
                player.sessionManager.getSessionFromPeriodUid(periodUid)?.let {
                    getMetricsForSession(it)?.let(this::notifyMetricsReady)
                }
            })
        }
    }

    override fun onStallChanged(eventTime: EventTime, isStall: Boolean) {
        if (isStall) {
            totalStallTimeCounter.play()
            stallCount++
        } else {
            totalStallTimeCounter.pause()
        }
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            totalPlaytimeCounter.play()
        } else {
            totalPlaytimeCounter.pause()
        }
    }

    override fun onBandwidthEstimate(eventTime: EventTime, totalLoadTimeMs: Int, totalBytesLoaded: Long, bitrateEstimate: Long) {
        bandwidth = bitrateEstimate
    }

    override fun onVideoInputFormatChanged(eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        videoFormat = format
    }

    override fun onVideoDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        videoFormat = null
    }

    override fun onAudioInputFormatChanged(eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        audioFormat = format
    }

    override fun onAudioDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        audioFormat = null
    }

    private fun updateStartupTimeWithState(eventTime: EventTime, state: Int) {
        println("[getOrCreateLoadingTimes] ${StringUtil.playerStateString(state)}")

        if (eventTime.timeline.isEmpty) return
        val periodUid = eventTime.getUidOfPeriod(window)
        val startupTimes = getOrCreateLoadingTimes(periodUid)
        startupTimes.state = state
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        println("[onPlaybackStateChanged] ${StringUtil.playerStateString(state)}")

        updateStartupTimeWithState(eventTime, state)
        when (state) {
            Player.STATE_BUFFERING -> {
                totalBufferingTimeCounter.play()
            }

            Player.STATE_READY -> {
                totalBufferingTimeCounter.pause()
            }
        }
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        println("[onRenderedFirstFrame] ${StringUtil.playerStateString(player.playbackState)}")

        updateStartupTimeWithState(eventTime, player.playbackState)
    }

    override fun onAudioPositionAdvancing(eventTime: EventTime, playoutStartSystemTimeMs: Long) {
        println("[onAudioPositionAdvancing] ${StringUtil.playerStateString(player.playbackState)}")

        updateStartupTimeWithState(eventTime, player.playbackState)
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        bufferDuration = player.totalBufferedDuration.milliseconds
    }

    override fun onLoadCompleted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        if (eventTime.timeline.isEmpty) return
        val periodUid = eventTime.getUidOfPeriod(window)
        val loadingTimes = getOrCreateLoadingTimes(periodUid)
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds
        when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> {
                if (loadingTimes.drm == null) {
                    loadingTimes.drm = loadDuration
                }
            }

            C.DATA_TYPE_MANIFEST -> {
                if (loadingTimes.manifest == null) {
                    loadingTimes.manifest = loadDuration
                }
            }

            C.DATA_TYPE_MEDIA -> {
                if (loadingTimes.source == null) {
                    loadingTimes.source = loadDuration
                }
            }

            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> {
                if (loadingTimes.asset == null) {
                    loadingTimes.asset = loadDuration
                }
            }

            else -> {
            }
        }
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        listeners.clear()
    }

    private fun computeBitrate(): Int {
        val videoBitrate = videoFormat?.bitrate ?: Format.NO_VALUE
        val audioBitrate = audioFormat?.bitrate ?: Format.NO_VALUE
        var bitrate = 0
        if (videoBitrate > 0) bitrate += videoBitrate
        if (audioBitrate > 0) bitrate += audioBitrate
        return bitrate
    }

    private fun reset() {
        println("[reset]")

        stallCount = 0
        totalStallTimeCounter.reset()
        totalPlaytimeCounter.reset()
        totalBufferingTimeCounter.reset()

        bufferDuration = Duration.ZERO

        audioFormat = player.audioFormat
        videoFormat = player.videoFormat
        if (player.isPlaying) {
            totalPlaytimeCounter.play()
        }
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

    /**
     * Get metrics for session
     *
     * @param session
     * @return
     */
    fun getMetricsForSession(session: PlaybackSessionManager.Session): PlaybackMetrics? {
        val loadingTimes = getOrCreateLoadingTimes(session.periodUid)
        return PlaybackMetrics(
            sessionId = session.sessionId,
            bandwidth = bandwidth,
            bitrate = computeBitrate(),
            bufferDuration = bufferDuration,
            playbackDuration = totalPlaytimeCounter.getTotalPlayTime(),
            stallCount = stallCount,
            stallDuration = totalStallTimeCounter.getTotalPlayTime(),
            loadDuration = loadingTimes.toLoadDuration()
        )
    }

    private companion object {
        const val TAG = "MetricsCollector"

        private fun LoadingTimes.toLoadDuration(): PlaybackMetrics.LoadDuration {
            return PlaybackMetrics.LoadDuration(
                source = source,
                manifest = manifest,
                drm = drm,
                asset = asset,
                timeToReady = timeToReady,
            )
        }
    }
}
