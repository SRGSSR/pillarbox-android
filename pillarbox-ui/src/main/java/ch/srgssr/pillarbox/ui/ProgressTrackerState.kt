/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * This interface allows subscribing to the current [Player] progress and provides callbacks for manual progress adjustments.
 */
interface ProgressTrackerState {
    /**
     * A [StateFlow] emitting the current progress, which can either be the progress manually set, or the actual progress of the underlying [Player].
     */
    val progress: StateFlow<Duration>

    /**
     * Callback to invoke when the progress is being manually changed.
     *
     * @param progress The new progress of the media being played. It must be between 0ms and the total duration of the media, as returned by
     * [Player.getDuration].
     */
    fun onChanged(progress: Duration)

    /**
     * Callback to invoke when the progress operation has finished.
     */
    fun onFinished()
}
