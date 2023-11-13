/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

/**
 * Tracker factory
 *
 * @constructor Create empty Tracker factory
 */
interface MediaItemTrackerProvider {
    /**
     * Get media item tracker factory
     *
     * @param trackerClass
     * @return
     */
    fun getMediaItemTrackerFactory(trackerClass: Class<*>): MediaItemTracker.Factory
}
