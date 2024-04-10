/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Pillarbox data
 *
 * @property trackersData The [MediaItemTrackerData].
 */
data class PillarboxData(
    val trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY,
) {
    companion object {
        /**
         * Empty [PillarboxData].
         */
        val EMPTY = PillarboxData()
    }
}
