/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.availableCommandsAsFlow
import ch.srgssr.pillarbox.player.canSeek
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.currentPositionPercent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Progress tracker
 *
 * Handle a progress position that is a mix of the player current position and the user desired seek position.
 *
 * @property player The player whose current position must be tracked.
 */
@Stable
class ProgressTracker internal constructor(private val player: Player) {
    private val playerProgressPercent: Flow<Float> = player.currentPositionAsFlow().map { player.currentPositionPercent() }
    private val userSeekState = MutableStateFlow<UserSeekState>(UserSeekState.Idle)
    private val canSeek = player.availableCommandsAsFlow().map { it.canSeek() }
    private val progressPercentFlow: Flow<Float> = combine(userSeekState, playerProgressPercent) { seekState, playerProgress ->
        when (seekState) {
            is UserSeekState.Seeking -> seekState.percent
            else -> playerProgress
        }
    }

    /**
     * Progress percent
     *
     * @return progress percent as State.
     */
    @Composable
    fun progressPercent(): State<Float> = progressPercentFlow.collectAsState(initial = player.currentPositionPercent())

    /**
     * Can seek
     *
     * @return can seek as State.
     */
    @Composable
    fun canSeek(): State<Boolean> = canSeek.collectAsState(initial = player.availableCommands.canSeek())

    /**
     * User seek at percent position
     *
     * @param percent Position in percent [0,1].
     */
    fun userSeek(percent: Float) {
        userSeekState.value = UserSeekState.Seeking(percent)
    }

    /**
     * User has finished seeking.
     */
    fun userSeekFinished() {
        userSeekState.value.let {
            if (it is UserSeekState.Seeking) {
                userSeekState.value = UserSeekState.End(it.percent)
            }
        }
    }

    internal suspend fun handleSeek() {
        userSeekState.collectLatest {
            when (it) {
                is UserSeekState.End -> {
                    player.seekTo((it.percent * player.duration).toLong())
                }

                else -> {
                    // Nothing
                }
            }
        }
    }

    private sealed interface UserSeekState {
        data object Idle : UserSeekState
        data class Seeking(val percent: Float) : UserSeekState
        data class End(val percent: Float) : UserSeekState
    }
}

/**
 * Remember progress tracker
 *
 * @param player The player to observe.
 */
@Composable
fun rememberProgressTracker(player: Player): ProgressTracker {
    val progressTracker = remember(player) {
        ProgressTracker(player)
    }
    LaunchedEffect(progressTracker) {
        progressTracker.handleSeek()
    }
    return progressTracker
}
