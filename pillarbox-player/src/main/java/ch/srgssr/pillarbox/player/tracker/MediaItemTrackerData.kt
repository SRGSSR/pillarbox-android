/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Represents a pairing of a [MediaItemTracker.Factory] and its associated data.
 *
 * @param T The type of data used by the factory.
 * @property factory The [MediaItemTracker.Factory] responsible for creating [MediaItemTracker].
 * @property data The data of type [T] that will be passed to the tracker's [start][MediaItemTracker.start] method.
 */
class FactoryData<T>(val factory: MediaItemTracker.Factory<T>, val data: T)

/**
 * Mutable representation of [MediaItemTrackerData] used to build and modify tracking data.
 *
 * @constructor Creates an empty [MutableMediaItemTrackerData] instance.
 */
class MutableMediaItemTrackerData : MutableMap<Any, FactoryData<*>> by mutableMapOf() {
    /**
     * Converts this object to an immutable [MediaItemTrackerData] instance.
     *
     * @return A new [MediaItemTrackerData] instance populated with data from this object.
     */
    fun toMediaItemTrackerData() = MediaItemTrackerData(this)

    companion object {
        /**
         * An empty instance of [MutableMediaItemTrackerData].
         */
        val EMPTY = MutableMediaItemTrackerData()
    }
}

/**
 * Immutable snapshot of the [MediaItemTracker]'s [FactoryData].
 */
class MediaItemTrackerData internal constructor(
    mutableMediaItemTrackerData: MutableMediaItemTrackerData
) : Map<Any, FactoryData<*>> by mutableMediaItemTrackerData
