/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import java.util.UUID

/**
 * Events dispatcher that notifies when specific events happen (related to a session, media playback, ...).
 */
interface QoSEventsDispatcher {
    /**
     * - A session is linked to the period inside the timeline, see [Timeline.getUidOfPeriod][androidx.media3.common.Timeline.getUidOfPeriod].
     * - A session is created when the player does something with a [MediaItem].
     * - A session is current if the media item associated with the session is the current [MediaItem].
     * - A session is finished when it is no longer the current session, or when the session is removed from the player.
     *
     * @property periodUid The period id from [Timeline.getUidOfPeriod][androidx.media3.common.Timeline.getUidOfPeriod] for [mediaItem].
     * @property mediaItem The [MediaItem] linked to the session.
     */
    data class Session(
        val periodUid: Any,
        val mediaItem: MediaItem,
    ) {
        /**
         * Unique session id.
         */
        val sessionId = UUID.randomUUID().toString()
    }

    /**
     * Listener to be notified for every event dispatched by [QoSEventsDispatcher].
     */
    interface Listener {
        /**
         * On session created
         *
         * @param session
         */
        fun onSessionCreated(session: Session)

        /**
         * On current session
         *
         * @param session
         */
        fun onCurrentSession(session: Session)

        fun onMediaStart(session: Session)

        fun onSeek(session: Session)

        fun onStall(session: Session)

        /**
         * On session finished
         *
         * @param session
         */
        fun onSessionFinished(session: Session)

        /**
         * On player released
         */
        fun onPlayerReleased()
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
