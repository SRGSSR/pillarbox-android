/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import ch.srgssr.pillarbox.player.PillarboxExoPlayer

/**
 * A tracker for media items played by an player.
 *
 * @param T The type of data associated with each tracked media item.
 */
interface MediaItemTracker<T> {

    /**
     * Initiates media tracking for the given player.
     *
     * @param player The tracked [PillarboxExoPlayer] instance.
     * @param data The data associated with the playback session.
     */
    fun start(player: PillarboxExoPlayer, data: T)

    /**
     * Stops media tracking for the given player.
     *
     * @param player The tracked [PillarboxExoPlayer] instance. The player's current state may already reflect the next item.
     */
    fun stop(player: PillarboxExoPlayer)

    /**
     * A factory interface for creating instances of [MediaItemTracker].
     *
     * @param T The type of data associated with the created tracker.
     */
    fun interface Factory<T> {

        /**
         * Creates a new instance of a [MediaItemTracker].
         *
         * @return A new instance of a [MediaItemTracker].
         */
        fun create(): MediaItemTracker<T>
    }
}
