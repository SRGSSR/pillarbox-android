/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Data holder stored as a tag on a [MediaItem].
 *
 * @property tag Any existing tag on the [MediaItem] will be saved here.
 * @property trackerData The [MediaItemTrackerData] to use.
 * @property blockedIntervals The list of blocked intervals in the current media.
 * @property eventIntervals The list of [TimeInterval]s available in the current media.
 */
data class PillarboxTag(
    val tag: Any? = null,
    val trackerData: MediaItemTrackerData? = null,
    val blockedIntervals: List<BlockedInterval> = emptyList(),
    val eventIntervals: List<TimeInterval> = emptyList(),
)

/**
 * Get the [MediaItem] tag as a [PillarboxTag].
 */
fun MediaItem.getPillarboxTag(): PillarboxTag {
    val tag = this.localConfiguration?.tag
    return if (tag is PillarboxTag) {
        tag
    } else {
        PillarboxTag(tag = tag)
    }
}
