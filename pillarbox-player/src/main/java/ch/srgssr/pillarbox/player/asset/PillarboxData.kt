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
 * @property blockedIntervals The [BlockedInterval] list.
 * @property chapters The [Chapter] list.
 * @property timeIntervals The [ActionableTimeInterval] list.
 */
data class PillarboxData(
    val trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY,
    val blockedIntervals: List<BlockedInterval> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val timeIntervals: List<ActionableTimeInterval> = emptyList(),
) {
    companion object {
        /**
         * Empty [PillarboxData].
         */
        val EMPTY = PillarboxData()
    }
}
