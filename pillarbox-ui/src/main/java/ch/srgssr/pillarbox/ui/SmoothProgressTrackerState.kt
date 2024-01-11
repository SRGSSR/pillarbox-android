/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
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
    private val player: PillarboxExoPlayer,
    coroutineScope: CoroutineScope
) : ProgressTrackerState {
    private var storedSeekParameters = player.seekParameters
    private var storedPlayWhenReady = player.playWhenReady
    private var storedSmoothSeeking = player.smoothSeekingEnabled
    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
    private var startChanging = false
    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (!startChanging) {
            startChanging = true
            storedPlayWhenReady = player.playWhenReady
            storedSmoothSeeking = player.smoothSeekingEnabled
            storedSeekParameters = player.seekParameters
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
            player.smoothSeekingEnabled = true
            player.playWhenReady = false
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        startChanging = false
        simpleProgressTrackerState.onFinished()
        player.playWhenReady = storedPlayWhenReady
        player.smoothSeekingEnabled = storedSmoothSeeking
        player.setSeekParameters(storedSeekParameters)
    }

    private companion object {
        private const val SEEKING_PLAYBACK_SPEED = 16f
    }
}
