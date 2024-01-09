/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.Pillarbox
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
    private var storedPlayWhenReady = player.playWhenReady
    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (!startChanging) {
            player.smoothSeekingEnabled = true
            startChanging = true
            storedPlayWhenReady = player.playWhenReady
            player.playWhenReady = false
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        simpleProgressTrackerState.onFinished()
        player.playWhenReady = storedPlayWhenReady
        startChanging = false
        player.smoothSeekingEnabled = false
    }

    private companion object {
        private const val SEEKING_PLAYBACK_SPEED = 16f
    }
}
