/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * SimpleProgressTrackerState
 *
 * Handle a progress position that is a mix of the player current position and the user desired [progress] position.
 *
 * @param player The player whose current position must be tracked.
 */
class SimpleProgressTrackerState(private val player: Player, scope: CoroutineScope) : ProgressTrackerState {
    private val currentPlayerPosition = player.currentPositionAsFlow(1.seconds).map { it.milliseconds }
    private val currentProgression = MutableStateFlow(0.milliseconds)
    private val isProgressing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val progress: StateFlow<Duration> = isProgressing.flatMapLatest { isSeeking ->
        if (isSeeking) {
            currentProgression
        } else {
            currentPlayerPosition
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), player.currentPosition.milliseconds)

    override fun onChanged(progress: Duration) {
        isProgressing.value = true
        currentProgression.value = progress
    }

    override fun onFinished() {
        player.seekTo(currentProgression.value.inWholeMilliseconds)
        isProgressing.value = false
    }
}
