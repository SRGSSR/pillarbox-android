/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager

/**
 * Events dispatcher that notifies when specific events happen (related to a session, media playback, ...).
 */
interface QoSEventsDispatcher {
    /**
     * Listener to be notified for every event dispatched by [QoSEventsDispatcher].
     */
    interface Listener {
        /**
         * On media start
         *
         * @param session
         */
        fun onMediaStart(session: PlaybackSessionManager.Session) = Unit

        /**
         * On seek
         *
         * @param session
         */
        fun onSeek(session: PlaybackSessionManager.Session) = Unit

        /**
         * On stall
         *
         * @param session
         */
        fun onStall(session: PlaybackSessionManager.Session) = Unit

        /**
         * On error
         *
         * @param session
         */
        fun onError(session: PlaybackSessionManager.Session) = Unit

        /**
         * On player released
         */
        fun onPlayerReleased() = Unit
    }

    /**
     * Register an [ExoPlayer] to this [QoSEventsDispatcher].
     */
    fun registerPlayer(player: ExoPlayer)

    /**
     * Unregister an [ExoPlayer] from this [QoSEventsDispatcher].
     */
    fun unregisterPlayer(player: ExoPlayer)

    /**
     * Add a [Listener] to this [QoSEventsDispatcher].
     */
    fun addListener(listener: Listener)

    /**
     * Remove a [Listener] from this [QoSEventsDispatcher].
     */
    fun removeListener(listener: Listener)
}
