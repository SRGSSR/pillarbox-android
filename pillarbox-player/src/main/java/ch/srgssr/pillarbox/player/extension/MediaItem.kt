/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Get [MediaItemTrackerData] or null if not set.
 *
 * @return null if localConfiguration.tag is null or tag is not a [MediaItemTrackerData].
 */
fun MediaItem.getMediaItemTrackerDataOrNull(): MediaItemTrackerData? {
    return localConfiguration?.tag as? MediaItemTrackerData
}

/**
 * @return current [MediaItemTrackerData] or create.
 */
fun MediaItem.getMediaItemTrackerData(): MediaItemTrackerData {
    return getMediaItemTrackerDataOrNull() ?: MediaItemTrackerData.EMPTY
}

/**
 * Set tracker data. This method should only be called if {@link #setUri} is passed a non-null value.
 * @see MediaItem.Builder.setTag
 * @param trackerData Set trackerData to [MediaItem.Builder.setTag].
 * @return [MediaItem.Builder] for convenience
 */
fun MediaItem.Builder.setTrackerData(trackerData: MediaItemTrackerData): MediaItem.Builder {
    setTag(trackerData)
    return this
}
