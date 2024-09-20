/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer

/**
 * Media item tracker
 */
interface MediaItemTracker<T> {

    /**
     * Start Media tracking.
     *
     * @param player The player to track.
     * @param data The data associated.
     */
    fun start(player: ExoPlayer, data: T)

    /**
     * Stop Media tracking.
     *
     * @param player The player tracked. The current player state may reflect the next item.
     */
    fun stop(player: ExoPlayer)

    /**
     * Factory
     */
    fun interface Factory<T> {

        /**
         * @return a new instance of a [MediaItemTracker]
         */
        fun create(): MediaItemTracker<T>
    }
}
