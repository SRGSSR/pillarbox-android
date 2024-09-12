/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.metrics

/**
 * Information about stalls.
 *
 * @property data The list of recorded stalls.
 * @property total The formatted total of stalls.
 */
data class Stalls(
    val data: List<Float>,
    val total: String,
) {
    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Empty [Stalls].
         */
        val Empty = Stalls(
            data = emptyList(),
            total = "",
        )
    }
}
