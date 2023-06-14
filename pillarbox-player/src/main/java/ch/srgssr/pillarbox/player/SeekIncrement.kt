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
        private val DefaultSeekBackIncrement = C.DEFAULT_SEEK_BACK_INCREMENT_MS.milliseconds
        private val DefaultSeekForwardIncrement = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS.milliseconds
    }
}
