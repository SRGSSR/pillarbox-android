/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.C
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Retrieves the [MediaItemTrackerData] associated with this [Tracks].
 *
 * @return The [MediaItemTrackerData] if found, `null` otherwise.
 */
fun Tracks.getMediaItemTrackerDataOrNull(): MediaItemTrackerData? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_TRACKERS
    }?.getTrackFormat(0)?.customData as? MediaItemTrackerData
}

/**
 * Retrieves the list of [BlockedTimeRange] associated with this [Tracks].
 *
 * @return The list of [BlockedTimeRange] if found, `null` otherwise.
 */
@Suppress("UNCHECKED_CAST")
fun Tracks.getBlockedTimeRangeOrNull(): List<BlockedTimeRange>? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_BLOCKED
    }?.getTrackFormat(0)?.customData as? List<BlockedTimeRange>
}

/**
 * Checks if this [Tracks] contains a track of type [image][C.TRACK_TYPE_IMAGE].
 *
 * @return Whether an image track is present.
 */
fun Tracks.containsImageTrack(): Boolean {
    return containsType(C.TRACK_TYPE_IMAGE)
}
