/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlin.time.Duration

class SmoothProgressTrackerState(private val player: Player, scope: CoroutineScope) : ProgressTrackerState {
    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, scope)
    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    private var isSeeking = false
    private var pendingSeek: Duration? = null
    private val playerSeekState = callbackFlow<Unit> {
        val listener = object : Player.Listener {
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
                if (playbackState == Player.STATE_READY && isSeeking) {
                    seekToPending()
                    isSeeking = false
                }
            }
        }
        player.addListener(listener)
        awaitClose {
            player.removeListener(listener)
        }
    }

    private var storedPlayWhenReady = player.playWhenReady
    private var startChanging = false

    init {
        if (player is ExoPlayer) {
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
        playerSeekState.launchIn(scope)
    }

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (isSeeking) {
            pendingSeek = progress
            return
        }
        if (!startChanging) {
            startChanging = true
            storedPlayWhenReady = player.playWhenReady
            player.playWhenReady = false
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon() // TODO store current parameters
                .setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                .build()
            player.setPlaybackSpeed(10f) // TODO Store current playerback speed
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        simpleProgressTrackerState.onFinished()
        isSeeking = false
        pendingSeek = null
        player.setPlaybackSpeed(1f)
        player.playWhenReady = storedPlayWhenReady
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setPreferredVideoRoleFlags(0)
            .build()

        startChanging = false
    }

    private fun seekToPending() {
        pendingSeek?.let {
            player.seekTo(it.inWholeMilliseconds)
        }
        // userStopSeek()
        pendingSeek = null
    }
}

private fun Player.isSeeking(): Flow<Event> = callbackFlow {
    var isSeeking = false
    val listener = object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                isSeeking = true
                trySend(Event.Seeking)
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (reason == Player.STATE_READY && isSeeking) {
                // Send SeekEvent
                isSeeking = false
                trySend(Event.SeekTo)
                trySend(Event.Idle)
            }
        }
    }
    addListener(listener)
    awaitClose {
        removeListener(listener)
    }
}

sealed interface Event {
    data object Seeking : Event
    data object SeekTo : Event
    data object Idle : Event
}
