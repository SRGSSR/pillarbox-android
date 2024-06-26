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
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.utils.StringUtil
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Stall tracker
 * # Definition of a Stall
 * A Stall occurs when the player is buffering during playback without user interaction.
 */
class StallTracker : AnalyticsListener {
    private var stallCount = 0
    private var lastStallTime = 0L
    private var stallDuration = 0L

    // IDLE -> READY -> SEEKING -> READY -> IDLE
    //               -> STALLED ->
    enum class State {
        IDLE,
        READY,
        STALLED,
        SEEKING,
    }

    private var state: State = State.IDLE
        set(value) {
            if (value != field) return
            if (value == State.STALLED) {
                lastStallTime = System.currentTimeMillis()
                stallCount++
            }
            if (field == State.STALLED) {
                stallDuration += System.currentTimeMillis() - lastStallTime
            }
            field = value
        }


    private fun reset() {
        if (state == State.STALLED) {
            stallDuration += System.currentTimeMillis() - lastStallTime
        }
        state = State.IDLE
        Log.d(TAG, "Reset: #Stalls = $stallCount duration = ${stallDuration.milliseconds}")
        stallCount = 0
        lastStallTime = 0L
        stallDuration = 0
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
            Log.d(TAG, "onPositionDiscontinuity $state")
            if (state != State.STALLED) {
                state = State.SEEKING
            }
        }
    }

    override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        Log.e(TAG, "onLoadComplete $state")
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        Log.e(TAG, "onLoadError $state $wasCanceled")
        if (state == State.READY || state == State.SEEKING) {
            state = State.STALLED
            stallCount++
            lastStallTime = System.currentTimeMillis()
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, playbackState: Int) {
        Log.d(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(playbackState)} state = $state")
        when (playbackState) {
            Player.STATE_READY -> {
                if (state == State.STALLED) {
                    stallDuration += System.currentTimeMillis() - lastStallTime
                    Log.d(TAG, "Stall end #Stalls = $stallCount duration = ${stallDuration.milliseconds}")
                }
                state = State.READY
            }

            Player.STATE_BUFFERING -> {
                if (state == State.READY) {
                    state = State.STALLED
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
