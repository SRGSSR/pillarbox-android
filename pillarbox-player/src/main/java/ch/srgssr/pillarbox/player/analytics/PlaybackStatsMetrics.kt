/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener

/**
 * Playback stats metrics
 * Compute playback stats metrics likes stalls, playtime, bitrate, etc..
 */
class PlaybackStatsMetrics : PillarboxAnalyticsListener, StallDetector.Listener {

    private var stallCount = 0
    private var lastStallTime = 0L
    private var stallDuration = 0L
    private var lastIsPlaying = 0L
    private var totalPlaytimeDuration = 0L

    override fun onStallChanged(isStall: Boolean) {
        if (isStall) {
            lastStallTime = System.currentTimeMillis()
            stallCount++
        } else {
            stallDuration += System.currentTimeMillis() - lastStallTime
        }
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            lastIsPlaying = System.currentTimeMillis()
        } else {
            totalPlaytimeDuration += System.currentTimeMillis() - lastIsPlaying
        }
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
            reset()
        }
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        reset()
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        reset()
    }

    fun reset() {
        stallCount = 0
        lastStallTime = 0
        lastIsPlaying = 0
        totalPlaytimeDuration = 0
    }

    fun getCurrentMetrics(): Any? = null
}
