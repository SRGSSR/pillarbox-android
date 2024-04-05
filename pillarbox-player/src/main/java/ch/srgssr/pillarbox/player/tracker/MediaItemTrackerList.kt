/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * This class holds a list of [MediaItemTracker].
 *
 * ```kotlin
 * val trackers = MediaItemTrackerList()
 * trackers.add(tracker)
 * trackers.addAll(tracker1, tracker2)
 * ```
 *
 * @constructor Create an empty `MediaItemTrackerList`.
 */
class MediaItemTrackerList internal constructor() : Iterable<MediaItemTracker> {
    private val trackers = mutableListOf<MediaItemTracker>()

    internal val trackerList: List<MediaItemTracker> = trackers

    /**
     * Add a tracker to the list. Each [tracker] type can only be added once to this [MediaItemTracker].
     *
     * @param tracker The tracker to add.
     * @return `true` if the tracker was successfully added, `false` otherwise.
     */
    fun add(tracker: MediaItemTracker): Boolean {
        return if (trackers.none { it::class.java == tracker::class.java }) {
            trackers.add(tracker)
        } else {
            false
        }
    }

    /**
     * Add multiple trackers at once to the list. Each [tracker] type can only be added once to this [MediaItemTracker].
     *
     * @param trackers The trackers to add.
     * @return `false` if one of the trackers was already added, `true` otherwise.
     */
    fun addAll(trackers: List<MediaItemTracker>): Boolean {
        var added = true
        for (tracker in trackers) {
            if (!add(tracker)) {
                added = false
            }
        }
        return added
    }

    /**
     * Clear the list of trackers.
     */
    fun clear() {
        trackers.clear()
    }

    /**
     * Check if the list of trackers is empty of not.
     *
     * @return `true` if the list is empty, `false` otherwise.
     */
    fun isEmpty(): Boolean {
        return trackers.isEmpty()
    }

    override fun iterator(): Iterator<MediaItemTracker> {
        return trackers.iterator()
    }
}
