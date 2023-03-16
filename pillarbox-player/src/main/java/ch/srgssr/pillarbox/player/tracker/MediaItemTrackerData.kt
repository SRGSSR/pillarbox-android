/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Tracker data
 *
 * @constructor Create empty Tracker data
 */
class MediaItemTrackerData {
    private val map = HashMap<Class<*>, Any?>()

    /**
     * Trackers
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
     * @param tracker
     * @return
     */
    fun getData(tracker: MediaItemTracker): Any? {
        return map[tracker::class.java]
    }

    /**
     * Put data
     *
     * @param clazz
     * @param data
     */
    fun putData(clazz: Class<*>, data: Any? = null) {
        map[clazz] = data
    }
}
