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
 * @property blockedIntervals
 * @property chapters
 * @property customTimeIntervals
 */
data class PillarboxData(
    val trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY,
    val blockedIntervals: List<BlockedSection> = emptyList(),
    val chapters: List<ChapterInterval> = emptyList(),
    val customTimeIntervals: List<TimeInterval> = emptyList(),
) {
    companion object {
        /**
         * Empty [PillarboxData].
         */
        val EMPTY = PillarboxData()
    }
}
