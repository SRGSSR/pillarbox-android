/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.DeviceInfo
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.DefaultUpdateInterval
import ch.srgssr.pillarbox.player.PillarboxPlayer
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
 * A [ProgressTrackerState] implementation that updates the [Player] progress.
 *
 * @param player The [Player] whose progress needs to be tracked.
 * @param coroutineScope The [CoroutineScope] used for managing [StateFlow]s.
 * @param useScrubbingMode Set to true to seek on each [onChanged] otherwise it seek only when [onFinished] is called.
 * @param updateInterval The time interval between progress update from the player.
 */
class SimpleProgressTrackerState(
    private val player: PillarboxPlayer,
    coroutineScope: CoroutineScope,
    private val useScrubbingMode: Boolean = false,
    private val updateInterval: Duration = DefaultUpdateInterval,
) : ProgressTrackerState {
    private val currentPlayerProgress = player.currentPositionAsFlow(updateInterval = updateInterval).map { it.milliseconds }
    private val currentProgress = MutableStateFlow(0.milliseconds)
    private val isScrubbing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val progress: StateFlow<Duration> = isScrubbing.flatMapLatest { isProgressing ->
        if (isProgressing) {
            currentProgress
        } else {
            currentPlayerProgress
        }
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), player.currentPosition.milliseconds)

    override fun onChanged(progress: Duration) {
        if (!isScrubbing.value) {
            player.setScrubbingModeEnabled(player.deviceInfo.playbackType != DeviceInfo.PLAYBACK_TYPE_REMOTE && useScrubbingMode)
            isScrubbing.value = true
        }
        currentProgress.value = progress
        if (player.isScrubbingModeEnabled()) {
            player.seekTo(progress.inWholeMilliseconds)
        }
    }

    override fun onFinished() {
        isScrubbing.value = false
        player.setScrubbingModeEnabled(false)
        player.seekTo(currentProgress.value.inWholeMilliseconds)
    }
}
