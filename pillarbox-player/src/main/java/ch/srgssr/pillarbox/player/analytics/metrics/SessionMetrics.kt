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
    private val loadingTimes = LoadingTimes(
        timeProvider = timeProvider,
        onLoadingReady = { sessionMetricsReady(this) },
    )
    private var currentPlaybackState: @Player.State Int = initialPlaybackState
    var videoFormat: Format? = null
    var audioFormat: Format? = null
    var stallCount: Int = 0
    var estimateBitrate: Long = 0
    var totalLoadTime: Duration = Duration.ZERO
    var totalBytesLoaded: Long = 0L
    val source: Duration?
        get() = loadingTimes.source
    val manifest: Duration?
        get() = loadingTimes.manifest
    val asset: Duration?
        get() = loadingTimes.asset
    val drm: Duration?
        get() = loadingTimes.drm
    val timeToReady: Duration?
        get() = loadingTimes.timeToReady
    val totalPlayingDuration: Duration
        get() = totalPlaytimeCounter.getTotalPlayTime()
    val totalBufferingDuration: Duration
        get() = totalBufferingTimeCounter.getTotalPlayTime()
    val totalStallDuration: Duration
        get() = totalStallTimeCounter.getTotalPlayTime()
    val totalDrmLoadingDuration: Duration?
        get() = totalDrmLoadingCounter.getTotalPlayTime().takeIf { it != Duration.ZERO }
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
            .sumOf { it.bitrate.coerceAtLeast(0) }
            .takeIf { it != 0 }
            ?.toLong()
            ?: Format.NO_VALUE.toLong()
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

        val fieldSetter = when (mediaLoadData.dataType) {
            // FIXME Never called!
            C.DATA_TYPE_DRM -> loadingTimes::drm
            C.DATA_TYPE_MANIFEST -> loadingTimes::manifest
            C.DATA_TYPE_MEDIA -> loadingTimes::source
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> loadingTimes::asset
            else -> return
        }

        if (fieldSetter.get() == null) {
            fieldSetter.set(loadDuration)
        }
    }
}
