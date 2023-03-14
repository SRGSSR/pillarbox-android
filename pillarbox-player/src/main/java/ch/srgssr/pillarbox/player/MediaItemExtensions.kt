/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerList

/**
 * Get trackers or null
 *
 * @return null if localConfiguration.tag is null or tag is not a [MediaItemTrackerList].
 */
@Suppress("SwallowedException")
fun MediaItem.getTrackersOrNull(): MediaItemTrackerList? {
    return try {
        return localConfiguration?.tag as MediaItemTrackerList?
    } catch (e: ClassCastException) {
        null
    }
}

/**
 * Get existing Trackers
 *
 * @return existing Trackers or create new empty one.
 */
fun MediaItem.getTrackers(): MediaItemTrackerList {
    return getTrackersOrNull() ?: MediaItemTrackerList()
}

/**
 * Append trackers and create a new Mediaitem. MediaItem is a immutable object.
 *
 * @param listTracker List of [MediaItemTracker] to append.
 * @return a new MediaItem with appended trackers to it.
 */
fun MediaItem.appendTrackers(vararg listTracker: MediaItemTracker): MediaItem {
    val trackers = getTrackers()
    trackers.appends(*listTracker)
    return buildUpon().setTag(trackers).build()
}
