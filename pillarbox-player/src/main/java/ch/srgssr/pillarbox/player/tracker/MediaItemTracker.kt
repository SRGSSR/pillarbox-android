/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer

/**
 * Media item tracker
 */
interface MediaItemTracker {

    /**
     * Start Media tracking.
     *
     * @param player The player to track.
     */
    fun start(player: ExoPlayer)

    /**
     * Stop Media tracking.
     *
     * @param player The player tracked.
     */
    fun stop(player: ExoPlayer)

    /**
     * Update with data.
     *
     * Data may not have change.
     *
     * @param data The data to use with this Tracker.
     */
    fun update(data: Any) {}

    /**
     * Factory
     */
    interface Factory {
        /**
         * Create a new instance of a [MediaItemTracker]
         *
         * @return a new instance.
         */
        fun create(): MediaItemTracker
    }
}
