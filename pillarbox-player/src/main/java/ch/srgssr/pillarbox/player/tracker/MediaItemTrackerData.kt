/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Immutable MediaItem tracker data.
 */
class MediaItemTrackerData private constructor(private val map: Map<Class<*>, Any?>) {

    /**
     * List of tracker class that have data.
     */
    val trackers: Collection<Class<*>>
        get() {
            return map.keys
        }

    /**
     * Is empty
     */
    val isEmpty: Boolean = map.isEmpty()

    /**
     * Is not empty
     */
    val isNotEmpty: Boolean = map.isNotEmpty()

    /**
     * Get data for a Tracker
     *
     * @param T The Data class.
     * @param tracker The tracker to retrieve the data.
     * @return data for tracker as T if it exist.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getDataAs(tracker: MediaItemTracker): T? {
        return map[tracker::class.java] as T?
    }

    /**
     * Get data for a tracker
     *
     * @param tracker The tracker to get data of.
     * @return generic data if any.
     */
    fun getData(tracker: MediaItemTracker): Any? {
        return map[tracker::class.java]
    }

    /**
     * Build upon
     *
     * @return A builder filled with current data.
     */
    fun buildUpon(): Builder = Builder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaItemTrackerData

        return map == other.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        return "MediaItemTrackerData(map=$map)"
    }

    companion object {
        /**
         * Empty [MediaItemTrackerData].
         */
        val EMPTY = MediaItemTrackerData(emptyMap())
    }

    /**
     * Builder
     *y
     * @param source set this builder with source value.
     */
    class Builder(source: MediaItemTrackerData = EMPTY) {
        private val map = HashMap<Class<*>, Any?>(source.map)

        /**
         * Put data for trackerClass
         *
         * @param T extends [MediaItemTracker].
         * @param trackerClass The class of the [MediaItemTracker].
         * @param data The data to associated with any instance of trackerClass.
         */
        fun <T : MediaItemTracker> putData(trackerClass: Class<T>, data: Any? = null): Builder {
            map[trackerClass] = data
            return this
        }

        /**
         * Build
         *
         * @return a new instance of [MediaItemTrackerData]
         */
        fun build(): MediaItemTrackerData = MediaItemTrackerData(map.toMap())
    }
}
