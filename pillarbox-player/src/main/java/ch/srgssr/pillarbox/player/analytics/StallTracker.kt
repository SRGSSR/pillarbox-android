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
    private var lastIsPlaying = 0L
    private var totalPlaytimeDuration = 0L

    private enum class State {
        IDLE,
        READY,
        STALLED,
        SEEKING,
    }

    private var state: State = State.IDLE
        set(value) {
            if (value == field) return
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
        state = State.IDLE

        Log.d(TAG, "Metrics: #Stalls = $stallCount duration = ${stallDuration.milliseconds} totalPlayTime = ${totalPlaytimeDuration.milliseconds}")
        stallCount = 0
        lastStallTime = 0L
        stallDuration = 0
        totalPlaytimeDuration = 0
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

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            lastIsPlaying = System.currentTimeMillis()
        } else {
            totalPlaytimeDuration += System.currentTimeMillis() - lastIsPlaying
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        val isNotStalled = state != State.STALLED
        val isSameMediaItem = oldPosition.mediaItemIndex == newPosition.mediaItemIndex
        val isSeekDiscontinuity = reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT

        if (isNotStalled && isSameMediaItem && isSeekDiscontinuity) {
            state = State.SEEKING
        }
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        if (state == State.READY || state == State.SEEKING) {
            state = State.STALLED
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                state = State.READY
            }

            Player.STATE_BUFFERING -> {
                if (state == State.READY) {
                    state = State.STALLED
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
