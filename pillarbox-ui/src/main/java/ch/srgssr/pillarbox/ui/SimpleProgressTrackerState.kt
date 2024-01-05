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

/**
 * [Player] progress tracker that only updated the player's actual progress when [onFinished] is called.
 *
 * @param player The [Player] whose current position must be tracked.
 * @param coroutineScope
 */
class SimpleProgressTrackerState(
    private val player: Player,
    coroutineScope: CoroutineScope
) : ProgressTrackerState {
    private val currentPlayerProgress = player.currentPositionAsFlow().map { it.milliseconds }
    private val currentProgress = MutableStateFlow(0.milliseconds)
    private val isProgressing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val progress: StateFlow<Duration> = isProgressing.flatMapLatest { isProgressing ->
        if (isProgressing) {
            currentProgress
        } else {
            currentPlayerProgress
        }
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), player.currentPosition.milliseconds)

    override fun onChanged(progress: Duration) {
        isProgressing.value = true
        currentProgress.value = progress
    }

    override fun onFinished() {
        isProgressing.value = false
        player.seekTo(currentProgress.value.inWholeMilliseconds)
    }
}
