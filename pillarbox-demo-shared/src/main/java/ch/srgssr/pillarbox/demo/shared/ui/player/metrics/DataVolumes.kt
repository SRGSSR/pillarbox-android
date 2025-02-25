/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.metrics

/**
 * Information about data volumes.
 *
 * @property data The list of recorded data volumes.
 * @property total The formatted total volume.
 */
data class DataVolumes(
    val data: List<Float>,
    val total: String,
) {
    /**
     * The unit in which the volumes are expressed.
     */
    val unit = "MByte"

    companion object {
        /**
         * Empty [DataVolumes].
         */
        val Empty = DataVolumes(
            data = emptyList(),
            total = "",
        )
    }
}
