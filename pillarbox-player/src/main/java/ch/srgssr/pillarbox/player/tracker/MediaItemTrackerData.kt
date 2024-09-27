/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Link between [data] and it's [factory].
 *
 * @param T The factory data type.
 * @property factory The [MediaItemTracker.Factory].
 * @property data The data of type T to use in [MediaItemTracker.start].
 */
class FactoryData<T>(val factory: MediaItemTracker.Factory<T>, val data: T)

/**
 * Mutable MediaItem tracker data.
 *
 * @constructor Create empty Mutable media item tracker data
 */
class MutableMediaItemTrackerData : MutableMap<Any, FactoryData<*>> by mutableMapOf() {
    /**
     * To media item tracker data
     */
    fun toMediaItemTrackerData() = MediaItemTrackerData(this)

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Empty mutable media item tracker data.
         */
        val EMPTY = MutableMediaItemTrackerData()
    }
}

/**
 * Immutable MediaItem tracker data.
 */
class MediaItemTrackerData internal constructor(
    mutableMediaItemTrackerData: MutableMediaItemTrackerData
) : Map<Any, FactoryData<*>> by mutableMediaItemTrackerData
