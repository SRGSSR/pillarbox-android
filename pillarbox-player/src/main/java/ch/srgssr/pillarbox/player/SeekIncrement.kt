/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Seek increment
 *
 * @property backward The seek back increment.
 * @property forward The seek forward increment.
 */
data class SeekIncrement(
    val backward: Duration = DefaultSeekBackIncrement,
    val forward: Duration = DefaultSeekForwardIncrement
) {

    init {
        require(backward > Duration.ZERO) { "Seek back increment have to be greater than zero" }
        require(forward > Duration.ZERO) { "Seek forward increment have to be greater than zero" }
    }

    companion object {
        private val DefaultSeekBackIncrement = C.DEFAULT_SEEK_BACK_INCREMENT_MS.milliseconds
        private val DefaultSeekForwardIncrement = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS.milliseconds
    }
}
