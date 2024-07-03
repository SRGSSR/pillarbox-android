/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Playback stats metrics
 * Compute playback stats metrics likes stalls, playtime, bitrate, etc..
 */
class PlaybackStatsMetrics(private val player: ExoPlayer) : PillarboxAnalyticsListener {

    private var stallCount = 0
    private var lastStallTime = 0L
    private var totalStallDuration = Duration.ZERO
    private var lastIsPlayingTime = if (player.isPlaying) System.currentTimeMillis() else 0L
    private var totalPlaytimeDuration = Duration.ZERO

    private var bandwidth = 0L
    private var bufferDuration = Duration.ZERO

    private var audioFormat: Format? = player.audioFormat
    private var videoFormat: Format? = player.videoFormat

    override fun onStallChanged(eventTime: AnalyticsListener.EventTime, isStall: Boolean) {
        if (isStall) {
            lastStallTime = System.currentTimeMillis()
            stallCount++
        } else {
            totalStallDuration += computeStallDuration()
            lastStallTime = 0
        }
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            lastIsPlayingTime = System.currentTimeMillis()
        } else {
            totalPlaytimeDuration += computePlaybackDuration()
            lastIsPlayingTime = 0
        }
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
            reset()
        }
    }

    override fun onBandwidthEstimate(eventTime: AnalyticsListener.EventTime, totalLoadTimeMs: Int, totalBytesLoaded: Long, bitrateEstimate: Long) {
        bandwidth = bitrateEstimate
    }

    override fun onVideoInputFormatChanged(eventTime: AnalyticsListener.EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        videoFormat = format
    }

    override fun onVideoDisabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        videoFormat = null
    }

    override fun onAudioInputFormatChanged(eventTime: AnalyticsListener.EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?) {
        audioFormat = format
    }

    override fun onAudioDisabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        audioFormat = null
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        bufferDuration = player.totalBufferedDuration.milliseconds
    }

    private fun computePlaybackDuration(): Duration {
        return if (lastIsPlayingTime > 0) (System.currentTimeMillis() - lastIsPlayingTime).milliseconds
        else Duration.ZERO
    }

    private fun computeStallDuration(): Duration {
        return if (lastStallTime > 0) (System.currentTimeMillis() - lastStallTime).milliseconds
        else Duration.ZERO
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
        stallCount = 0
        totalStallDuration = Duration.ZERO
        lastStallTime = 0

        lastIsPlayingTime = 0
        totalPlaytimeDuration = Duration.ZERO

        bufferDuration = Duration.ZERO

        audioFormat = player.audioFormat
        videoFormat = player.videoFormat
        if (player.isPlaying) {
            lastIsPlayingTime = System.currentTimeMillis()
        }
    }

    /**
     * Get current metrics
     *
     * @return metrics to the current time
     */
    fun getCurrentMetrics(): PlaybackStats {
        return PlaybackStats(
            bandwidth = bandwidth,
            bitrate = computeBitrate(),
            bufferDuration = bufferDuration,
            playbackDuration = totalPlaytimeDuration + computePlaybackDuration(),
            stallCount = stallCount,
            stallDuration = totalStallDuration + computeStallDuration()
        )
    }
}
