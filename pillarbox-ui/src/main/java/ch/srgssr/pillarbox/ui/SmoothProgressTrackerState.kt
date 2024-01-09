/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.Pillarbox
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * [Player] progress tracker that updates the player's actual progress everytime that [onChanged] is called.
 *
 * @param player The [Player] whose current position must be tracked.
 * @param coroutineScope
 */
class SmoothProgressTrackerState(
    private val player: Pillarbox,
    coroutineScope: CoroutineScope
) : ProgressTrackerState {

    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
    private var startChanging = false

    private var storedPlaybackSpeed = player.getPlaybackSpeed()
    private var storedPlayWhenReady = player.playWhenReady
    private var storedTrackSelectionParameters = player.trackSelectionParameters

    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (!startChanging) {
            player.smoothSeekingEnabled = true
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
        startChanging = false
        player.smoothSeekingEnabled = false
    }

    private companion object {
        private const val SEEKING_PLAYBACK_SPEED = 16f
    }
}
