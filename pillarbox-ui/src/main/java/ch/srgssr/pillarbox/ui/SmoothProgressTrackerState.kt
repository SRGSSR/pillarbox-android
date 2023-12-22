/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlin.time.Duration

/**
 * [Player] progress tracker that updates the player's actual progress everytime that [onChanged] is called.
 *
 * @param player The [Player] whose current position must be tracked.
 * @param coroutineScope
 */
class SmoothProgressTrackerState(
    private val player: Player,
    coroutineScope: CoroutineScope
) : ProgressTrackerState {
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

    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)

    private var isSeeking = false
    private var pendingSeek: Duration? = null
    private var startChanging = false

    private var storedPlaybackSpeed = player.getPlaybackSpeed()
    private var storedPlayWhenReady = player.playWhenReady
    private var storedTrackSelectionParameters = player.trackSelectionParameters

    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    init {
        if (player is ExoPlayer) {
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }

        playerSeekState.launchIn(coroutineScope)
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
            storedTrackSelectionParameters = player.trackSelectionParameters

            player.playWhenReady = false
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                .build()
            player.setPlaybackSpeed(SEEKING_PLAYBACK_SPEED)
        }

        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        simpleProgressTrackerState.onFinished()

        player.playWhenReady = storedPlayWhenReady
        player.trackSelectionParameters = storedTrackSelectionParameters
        player.setPlaybackSpeed(storedPlaybackSpeed)

        isSeeking = false
        pendingSeek = null
        startChanging = false
    }

    private fun seekToPending() {
        pendingSeek?.let {
            player.seekTo(it.inWholeMilliseconds)
            pendingSeek = null
        }
    }

    private companion object {
        private const val SEEKING_PLAYBACK_SPEED = 10f
    }
}
