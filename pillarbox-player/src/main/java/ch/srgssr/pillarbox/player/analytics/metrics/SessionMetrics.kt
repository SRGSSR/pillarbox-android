/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.TotalPlaytimeCounter
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class SessionMetrics internal constructor(
    timeProvider: () -> Long = { System.currentTimeMillis() },
    initialPlaybackState: @Player.State Int = Player.STATE_IDLE,
    sessionMetricsReady: (SessionMetrics) -> Unit,
) {
    private var drmSessionStartedCounter = 0
    private val totalPlaytimeCounter = TotalPlaytimeCounter(timeProvider)
    private val totalStallTimeCounter = TotalPlaytimeCounter(timeProvider)
    private val totalBufferingTimeCounter = TotalPlaytimeCounter(timeProvider)
    private val totalDrmLoadingCounter = TotalPlaytimeCounter(timeProvider)
    private val loadingTimes: LoadingTimes = LoadingTimes(timeProvider = timeProvider, onLoadingReady = {
        sessionMetricsReady(this)
    })
    private var currentPlaybackState: @Player.State Int = initialPlaybackState
    var videoFormat: Format? = null
    var audioFormat: Format? = null
    var stallCount: Int = 0
    var estimateBitrate: Long = 0
    var totalLoadTime: Duration = Duration.ZERO
    var totalBytesLoaded: Long = 0L
    val source: Duration?
        get() {
            return loadingTimes.source
        }
    val manifest: Duration?
        get() {
            return loadingTimes.manifest
        }
    val asset: Duration?
        get() {
            return loadingTimes.asset
        }
    val drm: Duration?
        get() {
            return loadingTimes.drm
        }
    val timeToReady: Duration?
        get() {
            return loadingTimes.timeToReady
        }
    val totalPlayingDuration: Duration
        get() {
            return totalPlaytimeCounter.getTotalPlayTime()
        }
    val totalBufferingDuration: Duration
        get() {
            return totalBufferingTimeCounter.getTotalPlayTime()
        }
    val totalStallDuration: Duration
        get() {
            return totalStallTimeCounter.getTotalPlayTime()
        }
    val totalDrmLoadingDuration: Duration?
        get() {
            val duration = totalDrmLoadingCounter.getTotalPlayTime()
            return if (duration == Duration.ZERO) null else duration
        }
    var url: Uri? = null

    fun setDrmSessionAcquired() {
        if (drmSessionStartedCounter == 0) {
            totalDrmLoadingCounter.play()
        }
        drmSessionStartedCounter++
    }

    fun setDrmKeyLoaded() {
        drmSessionStartedCounter--
        if (drmSessionStartedCounter <= 0) {
            totalDrmLoadingCounter.pause()
            loadingTimes.state = currentPlaybackState
            drmSessionStartedCounter = 0
        }
    }

    fun getTotalBitrate(): Long {
        return listOfNotNull(videoFormat, audioFormat)
            .sumOf { it.bitrate }
            .toLong()
    }

    fun setBandwidthEstimate(totalLoadTimeMs: Int, totalBytesLoaded: Long, estimateBitrate: Long) {
        this.estimateBitrate = estimateBitrate
        this.totalLoadTime += totalLoadTimeMs.milliseconds
        this.totalBytesLoaded += totalBytesLoaded
    }

    fun setIsStall(isStall: Boolean) {
        if (isStall) {
            stallCount++
            totalStallTimeCounter.play()
        } else {
            totalStallTimeCounter.pause()
        }
    }

    fun setIsPlaying(playing: Boolean) {
        if (playing) {
            totalPlaytimeCounter.play()
        } else {
            totalPlaytimeCounter.pause()
        }
    }

    fun setRenderFirstFrameOrAudioPositionAdvancing() {
        loadingTimes.state = currentPlaybackState
    }

    fun setPlaybackState(state: Int) {
        currentPlaybackState = state
        if (drmSessionStartedCounter == 0) {
            loadingTimes.state = state
        }
        when (state) {
            Player.STATE_BUFFERING -> {
                totalBufferingTimeCounter.play()
            }

            Player.STATE_READY -> {
                totalBufferingTimeCounter.pause()
            }
        }
    }

    fun setLoadStarted(loadEventInfo: LoadEventInfo) {
        this.url = loadEventInfo.uri
    }

    /**
     * Should be called when [AnalyticsListener.onLoadCompleted] is called
     *
     * @param loadEventInfo The [LoadEventInfo].
     * @param mediaLoadData The [MediaLoadData].
     */
    fun setLoadCompleted(loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds
        this.url = loadEventInfo.uri
        when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> {
                // FIXME Never called!
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
}
