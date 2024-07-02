/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.exoplayer.analytics.AnalyticsListener

/**
 * Playback stats metrics
 * Compute playback stats metrics likes stalls, playtime, bitrate, etc..
 */
class PlaybackStatsMetrics : PillarboxAnalyticsListener {

    override fun onStallChanged(eventTime: AnalyticsListener.EventTime, isStalls: Boolean) {
        TODO("Not yet implemented")
    }

    fun getCurrentMetrics(): Any? = null
}
