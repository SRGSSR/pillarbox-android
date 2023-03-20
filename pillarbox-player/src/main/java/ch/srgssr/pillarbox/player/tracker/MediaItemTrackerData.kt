/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * MediaItem tracker data.
 *
 * @constructor Create empty Tracker data
 */
class MediaItemTrackerData {
    private val map = HashMap<Class<*>, Any?>()

    /**
     * List of tracker class that have data.
     */
    val trackers: Collection<Class<*>>
        get() {
            return map.keys
        }

    /**
     * Get data for
     *
     * @param T
     * @param tracker
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getDataFor(tracker: MediaItemTracker): T? {
        return map[tracker::class.java] as T?
    }

    /**
     * Get data
     *
     * @param tracker The tracker to get data of.
     * @return generic data if any.
     */
    fun getData(tracker: MediaItemTracker): Any? {
        return map[tracker::class.java]
    }

    /**
     * Put data for trackerClass
     *
     * @param T extends [MediaItemTracker].
     * @param trackerClass The class of the [MediaItemTracker].
     * @param data The data to associated with any instance of trackerClass.
     */
    fun <T : MediaItemTracker> putData(trackerClass: Class<T>, data: Any? = null) {
        map[trackerClass] = data
    }
}
