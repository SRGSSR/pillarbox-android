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
 * @return [MediaItemTrackerData] if it exists, `null` otherwise
 */
fun Tracks.getMediaItemTrackerDataOrNull(): MediaItemTrackerData? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_TRACKERS
    }?.getTrackFormat(0)?.customData as? MediaItemTrackerData
}

/**
 * @return a list of [BlockedTimeRange] if it exists, `null` otherwise
 */
@Suppress("UNCHECKED_CAST")
fun Tracks.getBlockedTimeRangeOrNull(): List<BlockedTimeRange>? {
    return groups.firstOrNull {
        it.type == PillarboxMediaSource.TRACK_TYPE_PILLARBOX_BLOCKED
    }?.getTrackFormat(0)?.customData as? List<BlockedTimeRange>
}

/**
 * Contains image track
 *
 * @return true if there is a track of type [C.TRACK_TYPE_IMAGE], false otherwise
 */
fun Tracks.containsImageTrack(): Boolean {
    return containsType(C.TRACK_TYPE_IMAGE)
}
