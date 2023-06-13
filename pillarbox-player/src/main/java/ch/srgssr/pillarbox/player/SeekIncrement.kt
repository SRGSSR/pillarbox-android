/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Seek increment
 *
 * @property seekBackIncrement Seek back increment.
 * @property seekForwardIncrement Seek forward increment
 */
data class SeekIncrement(
    val seekBackIncrement: Duration = DefaultSeekBackIncrement,
    val seekForwardIncrement: Duration = DefaultSeekForwardIncrement
) {

    init {
        require(seekBackIncrement > Duration.ZERO) { "Seek back increment have to be greater than zero" }
        require(seekForwardIncrement > Duration.ZERO) { "Seek forward increment have to be greater than zero" }
    }

    companion object {
        private val DefaultSeekBackIncrement = 5L.seconds
        private val DefaultSeekForwardIncrement = 10L.seconds
    }
}
