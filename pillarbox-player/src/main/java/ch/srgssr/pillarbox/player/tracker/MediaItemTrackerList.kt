/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Trackers hold a list of [MediaItemTracker].
 *
 *  val trackers = mediaItem.getTrackers()
 *  trackers.append(trackerA)
 *
 * @constructor Create empty Trackers.
 */
class MediaItemTrackerList internal constructor() : Iterable<MediaItemTracker> {
    private val listTracker = mutableListOf<MediaItemTracker>()

    /**
     * Immutable list of [MediaItemTracker].
     */
    val list: List<MediaItemTracker> = listTracker

    /**
     * The number of [MediaItemTracker] appended.
     */
    val size: Int
        get() = list.size

    /**
     * Append tracker to the list. You can append only one type of Tracker.
     *
     * @param tracker The track to add.
     * @return true if the tracker was successfully added, false otherwise.
     */
    fun append(tracker: MediaItemTracker): Boolean {
        if (listTracker.none { it::class.java == tracker::class.java }) {
            listTracker.add(tracker)
            return true
        }
        return false
    }

    /**
     * Appends multiple MediaTracker at once.
     *
     * @param trackers The MediaTracker list to append.
     * @return false if one of the trackers is already added.
     */
    fun appends(vararg trackers: MediaItemTracker): Boolean {
        var added = true
        for (tracker in trackers) {
            val currentAdded = append(tracker)
            if (!currentAdded) {
                added = false
            }
        }
        return added
    }

    /**
     * Find [MediaItemTracker] from T
     *
     * @param T The [MediaItemTracker] type to find.
     * @param trackerClass The class to find.
     * @return null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : MediaItemTracker> findTracker(trackerClass: Class<T>): T? {
        return listTracker.find { it::class.java == trackerClass } as T?
    }

    override fun iterator(): Iterator<MediaItemTracker> {
        return list.iterator()
    }
}
