/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.time.Duration.Companion.milliseconds

fun Player.isSeekingAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                trySend(true)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                trySend(false)
            }
        }
    }
}

class FasterSeeker(private val player: Player) : Player.Listener {
    private var isSeeking = false
    private var pendingSeek: Long? = null

    init {
        if (player is ExoPlayer) {
            Log.e(TAG, "Snap to key frame")
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
    }

    // User seek with his little fingers
    fun seekTo(position: Long) {
        if (isSeeking) {
            pendingSeek = position
            return
        }
        Log.d(TAG, "seek to ${position.milliseconds}")
        player.seekTo(position)
    }

    fun userStopSeek() {
        // DO nothing
        isSeeking = false
        pendingSeek?.let {
            player.seekTo(it)
        }
        pendingSeek = null
    }

    fun seekProcessed() {
        pendingSeek?.let {
            player.seekTo(it)
        }
        // userStopSeek()
        pendingSeek = null
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
            isSeeking = true
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            if (isSeeking) {
                seekProcessed()
                isSeeking = false
            }
        }
    }

    companion object {
        private const val TAG = "Seeker"
    }
}
