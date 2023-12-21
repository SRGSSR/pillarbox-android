/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface ProgressTrackerState {
    val progress: StateFlow<Duration>

    fun onChanged(progress: Duration)

    fun onFinished()
}
