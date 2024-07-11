/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.MediaItem
import java.util.UUID

/**
 * Playback session manager
 *
 * @constructor Create empty Playback session manager
 */
class PlaybackSessionManager : PillarboxAnalyticsListener {

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
     * Listener
     *
     * @constructor Create empty Listener
     */
    interface Listener {
        /**
         * On session created
         *
         * @param session
         */
        fun onSessionCreated(session: Session) = Unit

        /**
         * On current session
         *
         * @param session
         */
        fun onCurrentSession(session: Session) = Unit

        /**
         * On session finished
         *
         * @param session
         */
        fun onSessionFinished(session: Session) = Unit
    }

    /**
     * Add listener
     *
     * @param listener
     */
    fun addListener(listener: Listener) {
        TODO("Implement addListener")
    }

    /**
     * Remove listener
     *
     * @param listener
     */
    fun removeListener(listener: Listener) {
        TODO("implement removeListener")
    }

    /**
     * Get current session
     *
     * @return
     */
    fun getCurrentSession(): Session? {
        TODO("implement getCurrentSession")
    }

    /**
     * Get session from id
     *
     * @param sessionId
     * @return
     */
    fun getSessionFromId(sessionId: String): Session? {
        TODO("implement getSessionFromId")
    }
}
