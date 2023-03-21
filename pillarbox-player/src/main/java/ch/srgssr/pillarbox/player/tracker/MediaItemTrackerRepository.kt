/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Media item media item tracker repository
 *
 * @constructor Create empty Media item media item tracker repository
 */
class MediaItemTrackerRepository : MediaItemTrackerProvider {
    private val map = HashMap<Class<*>, MediaItemTracker.Factory>()

    /**
     * Register factory
     *
     * @param T Class type extends [MediaItemTracker]
     * @param trackerClass The class the trackerFactory create. Clazz must extends MediaItemTracker.
     * @param trackerFactory The tracker factory associated with clazz.
     */
    fun <T : MediaItemTracker> registerFactory(trackerClass: Class<T>, trackerFactory: MediaItemTracker.Factory) {
        map[trackerClass] = trackerFactory
    }

    override fun getMediaItemTrackerFactory(trackerClass: Class<*>): MediaItemTracker.Factory {
        assert(map.contains(trackerClass)) { "No MediaItemTracker.Factory found for $trackerClass" }
        return map[trackerClass]!!
    }
}
