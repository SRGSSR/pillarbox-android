/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer

/**
 * Media item tracker
 */
interface MediaItemTracker {

    /**
     * Stop reason
     */
    enum class StopReason {

        /**
         * When the player has been stopped, released or its current media item changes.
         */
        Stop,

        /**
         * When the player reaches the end of the media.
         */
        EoF
    }

    /**
     * Start Media tracking.
     *
     * @param player The player to track.
     * @param initialData The data associated if any.
     */
    fun start(player: ExoPlayer, initialData: Any?)

    /**
     * Stop Media tracking.
     *
     * @param player The player tracked.
     * @param reason To tell how the track is stopped.
     * @param positionMs The player position when the tracker is stopped.
     */
    fun stop(player: ExoPlayer, reason: StopReason, positionMs: Long)

    /**
     * Factory
     */
    fun interface Factory {
        /**
         * Create a new instance of a [MediaItemTracker]
         *
         * @return a new instance.
         */
        fun create(): MediaItemTracker
    }
}
