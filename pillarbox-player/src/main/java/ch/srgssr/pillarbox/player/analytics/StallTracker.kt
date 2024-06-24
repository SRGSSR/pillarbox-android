/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import kotlin.time.Duration.Companion.milliseconds

class StallTracker : AnalyticsListener {

    private var wasReadyOnce = false
    private var isSeeking = false
    private var stallCount = 0
    private var lastStallTime = 0L
    private var stallDuration = 0L

    private fun reset() {
        Log.d(TAG, "#Stalls = $stallCount duration = ${stallDuration.milliseconds}")
        isSeeking = false
        stallCount = 0
        lastStallTime = 0L
        wasReadyOnce = false
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        reset()
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        reset()
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        reset()
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (
            oldPosition.mediaItemIndex == newPosition.mediaItemIndex &&
            reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
        ) {
            isSeeking = true
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        when (state) {
            Player.STATE_READY -> {
                if (!isSeeking && wasReadyOnce) {
                    stallDuration += System.currentTimeMillis() - lastStallTime
                }
                isSeeking = false
                wasReadyOnce = true
            }

            Player.STATE_BUFFERING -> {
                if (!isSeeking && wasReadyOnce) {
                    stallCount++
                    lastStallTime = System.currentTimeMillis()
                    Log.d(TAG, "detect stall => #stalls = $stallCount")
                }
            }

            else -> {
                reset()
            }
        }
    }

    companion object {
        private const val TAG = "Stalls"
    }
}
