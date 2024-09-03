/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager

/**
 * Media item tracker
 */
interface MediaItemTracker {

    /**
     * Stop reason
     */
    enum class StopReason {
        Stop, EoF
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
     * Update with data.
     *
     * Data may not have change.
     *
     * @param data The data to use with this Tracker.
     */
    // fun update(data: Any) {}

    // Called when player start doing something with the item
    fun created(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) = Unit

    // Called when the item is current
    fun start(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) = Unit

    // Called when the item is no more current.
    fun stop(session: PlaybackSessionManager.SessionInfo, player: PillarboxExoPlayer) = Unit

    // The item is no more in the player. FIXME or like session, cleared is called after stop or when removed from playlist.
    fun cleared(session: PlaybackSessionManager.Session, player: PillarboxExoPlayer) = Unit

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
