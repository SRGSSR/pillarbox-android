/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import java.io.IOException

/**
 * Stall detector
 *
 * A Stall occurs when the player is [Player.STATE_BUFFERING] after being [Player.STATE_READY] during playback without user interactions.
 */
internal class StallDetector : AnalyticsListener {

    /**
     * Listener
     */
    interface Listener {
        /**
         * Called when the player stall state changed.
         *
         * @param isStall the stall state.
         */
        fun onStallChanged(isStall: Boolean)
    }

    private enum class State {
        IDLE,
        READY,
        STALLED,
        SEEKING,
    }

    private val listeners = mutableSetOf<Listener>()

    private var state: State = State.IDLE
        set(value) {
            if (value == field) return
            if (field == State.STALLED) {
                notifyStall(false)
            }
            if (value == State.STALLED) {
                notifyStall(true)
            }
            field = value
        }

    /**
     * Add listener
     *
     * @param listener The [Listener]
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /**
     * Remove listener
     *
     * @param listener The [Listener]
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyStall(isStall: Boolean) {
        HashSet(listeners).forEach {
            it.onStallChanged(isStall)
        }
    }

    private fun reset() {
        state = State.IDLE
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
