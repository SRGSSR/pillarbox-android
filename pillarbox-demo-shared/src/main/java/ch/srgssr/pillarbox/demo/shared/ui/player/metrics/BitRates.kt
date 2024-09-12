/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.metrics

/**
 * Information about bit rates.
 *
 * @property data The list of recorded bit rates.
 */
data class BitRates(
    val data: List<Float>,
) {
    /**
     * The unit in which the bit rates are expressed.
     */
    val unit = "Mbps"

    /**
     * The current bit rate.
     */
    val current: Float
        get() = data.last()

    /**
     * The biggest recorded bit rate.
     */
    val max: Float
        get() = data.max()

    /**
     * The smallest recorded bit rate.
     */
    val min: Float
        get() = data.min()

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Empty [BitRates].
         */
        val Empty = BitRates(
            data = emptyList(),
        )
    }
}
