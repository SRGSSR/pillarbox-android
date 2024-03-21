/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

data class PillarboxTag(
    val tag: Any? = null,
    val trackerData: MediaItemTrackerData? = null,
    val blockedIntervals: List<BlockedInterval> = emptyList(),
    val eventIntervals: List<TimeInterval> = emptyList(),
)

fun MediaItem?.getPillarboxTag(): PillarboxTag? {
    if (this == null) return null
    val tag = this.localConfiguration?.tag
    if (tag is PillarboxTag) return tag
    return PillarboxTag(tag = tag)
}
