/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Interface used to subscribe to and update the [Player] progression.
 */
interface ProgressTrackerState {
    /**
     * Emits the current progress, which can be either the value being manually set, or the actual [Player] progress.
     */
    val progress: StateFlow<Duration>

    /**
     * Callback to invoke when the progress is being manually changed.
     *
     * @param progress The new progress of the media being played. It must be between 0ms and [Player.getDuration].
     */
    fun onChanged(progress: Duration)

    /**
     * Callback to invoke when the progress is no longer being changed.
     */
    fun onFinished()
}
