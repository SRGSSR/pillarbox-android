/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.TimelineChangeReason
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import java.util.UUID

/**
 * Playback session manager
 *
 * @constructor Create empty Playback session manager
 */
class PlaybackSessionManager {
    /**
     * - A session is linked to the period inside the timeline, see [Timeline.getUidOfPeriod][androidx.media3.common.Timeline.getUidOfPeriod].
     * - A session is created when the player does something with a [MediaItem].
     * - A session is current if the media item associated with the session is the current [MediaItem].
     * - A session is destroyed when it is no longer the current session, or when the session is removed from the player.
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
     * Session info
     *
     * @property session The [Session]
     * @property position The position in milliseconds when a session change occurs.
     * @property positionTimestamp The timestamp associated with [position], if available.
     */
    data class SessionInfo(
        val session: Session,
        val position: Long,
        val positionTimestamp: Long?,
    )

    /**
     * Listener
     */
    interface Listener {
        /**
         * On session created
         *
         * @param session The newly created [Session].
         */
        fun onSessionCreated(session: Session) = Unit

        /**
         * On session destroyed. The session won't be current anymore.
         *
         * @param session The destroyed [Session].
         */
        fun onSessionDestroyed(session: Session) = Unit

        /**
         * On current session changed from [oldSession] to [newSession].
         * [onSessionDestroyed] with [oldSession] is called right after.
         *
         * @param oldSession The current session, if any.
         * @param newSession The next current session, if any.
         */
        fun onCurrentSessionChanged(
            oldSession: SessionInfo?,
            newSession: SessionInfo?,
        ) = Unit
    }

    private val analyticsListener = SessionManagerAnalyticsListener()
    private val listeners = mutableSetOf<Listener>()
    private val sessions = mutableMapOf<Any, Session>()
    private val window = Timeline.Window()

    private var _currentSession: Session? = null

    private fun setCurrentSession(newSession: SessionInfo?, oldPosition: Long, oldPositionTimestamp: Long?) {
        if (_currentSession == newSession?.session) {
            return
        }

        val oldSession = _currentSession
        _currentSession = newSession?.session
        notifyListeners {
            onCurrentSessionChanged(oldSession?.let { SessionInfo(it, oldPosition, oldPositionTimestamp) }, newSession)
        }
        // Clear session
        oldSession?.let { session ->
            notifyListeners { onSessionDestroyed(session) }
            sessions.remove(session.periodUid)
        }
    }

    /**
     * Set the player
     *
     * @param player
     */
    fun setPlayer(player: ExoPlayer) {
        player.addAnalyticsListener(analyticsListener)
    }

    /**
     * Add listener
     *
     * @param listener
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /**
     * Remove listener
     *
     * @param listener
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * Get current session
     *
     * @return
     */
    fun getCurrentSession(): Session? {
        return _currentSession
    }

    /**
     * Get session from id
     *
     * @param sessionId
     * @return
     */
    fun getSessionById(sessionId: String): Session? {
        return sessions.values.find { it.sessionId == sessionId }
    }

    /**
     * Get session from event time
     *
     * @param eventTime The [AnalyticsListener.EventTime].
     */
    fun getSessionFromEventTime(eventTime: EventTime): Session? {
        if (eventTime.timeline.isEmpty) {
            return null
        }

        eventTime.timeline.getWindow(eventTime.windowIndex, window)
        val periodUid = eventTime.timeline.getUidOfPeriod(window.firstPeriodIndex)
        return getSessionFromPeriodUid(periodUid)
    }

    /**
     * Get session from a period uid
     *
     * @param periodUid The period uid.
     */
    fun getSessionFromPeriodUid(periodUid: Any): Session? {
        return sessions[periodUid]
    }

    private inline fun notifyListeners(event: Listener.() -> Unit) {
        listeners.toList()
            .forEach { listener ->
                listener.event()
            }
    }

    private inner class SessionManagerAnalyticsListener : PillarboxAnalyticsListener {
        override fun onPositionDiscontinuity(
            eventTime: EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @DiscontinuityReason reason: Int
        ) {
            DebugLogger.debug(TAG, "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)}")

            val oldPositionTimestamp = eventTime.getPositionTimestamp(oldPosition.positionMs, window)
            val newPositionTimestamp = eventTime.getPositionTimestamp(newPosition.positionMs, window)

            if (reason == Player.DISCONTINUITY_REASON_REMOVE) {
                val newSession = newPosition.periodUid
                    ?.let(::getSessionFromPeriodUid)
                    ?.let { SessionInfo(it, newPosition.positionMs, newPositionTimestamp) }

                setCurrentSession(newSession, oldPosition.positionMs, oldPositionTimestamp)
            } else if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex && !eventTime.timeline.isEmpty) {
                val newSession = checkNotNull(getOrCreateSession(eventTime)) // Return null only if timeline is empty
                setCurrentSession(SessionInfo(newSession, newPosition.positionMs, newPositionTimestamp), oldPosition.positionMs, oldPositionTimestamp)
            }
        }

        override fun onTimelineChanged(
            eventTime: EventTime,
            @TimelineChangeReason reason: Int,
        ) {
            val timeline = eventTime.timeline
            val mediaItem = if (timeline.isEmpty) {
                MediaItem.EMPTY
            } else {
                timeline.getWindow(eventTime.windowIndex, window).mediaItem
            }

            DebugLogger.debug(TAG, "onTimelineChanged reason = ${StringUtil.timelineChangeReasonString(reason)} ${mediaItem.mediaMetadata.title}")

            if (timeline.isEmpty) {
                finishAllSessions()
                return
            }

            // Finish sessions that are no longer in the timeline
            val currentSessions = sessions.values.toSet()
            currentSessions.forEach { session ->
                val periodUid = session.periodUid
                val periodIndex = timeline.getIndexOfPeriod(periodUid)
                if (periodIndex == C.INDEX_UNSET && session != _currentSession) {
                    notifyListeners { onSessionDestroyed(session) }
                    sessions.remove(session.periodUid)
                }
            }
        }

        override fun onLoadStarted(
            eventTime: EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            getOrCreateSession(eventTime)
        }

        override fun onPlayerError(
            eventTime: EventTime,
            error: PlaybackException,
        ) {
            DebugLogger.debug(TAG, "onPlayerError", error)
            getOrCreateSession(eventTime)
        }

        override fun onPlayerReleased(eventTime: EventTime) {
            DebugLogger.debug(TAG, "onPlayerReleased")
            finishAllSessions(eventTime)
            listeners.clear()
        }

        override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
            DebugLogger.debug(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)}")
            when (state) {
                Player.STATE_IDLE,
                Player.STATE_ENDED -> finishAllSessions(eventTime)

                else -> getOrCreateSession(eventTime)
            }
        }

        private fun getOrCreateSession(eventTime: EventTime): Session? {
            if (eventTime.timeline.isEmpty) {
                return null
            }
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            val periodUid = eventTime.getUidOfPeriod(window)
            var session = getSessionFromPeriodUid(periodUid)
            if (session == null) {
                val newSession = Session(periodUid, window.mediaItem)
                sessions[periodUid] = newSession
                notifyListeners { onSessionCreated(newSession) }

                if (_currentSession == null) {
                    val position = eventTime.currentPlaybackPositionMs
                    val positionTimestamp = eventTime.getPositionTimestamp(eventTime.currentPlaybackPositionMs, window)

                    setCurrentSession(SessionInfo(newSession, position, positionTimestamp), C.TIME_UNSET, null)
                }

                session = newSession
            }

            return session
        }

        private fun finishAllSessions(eventTime: EventTime? = null) {
            if (eventTime != null) {
                val position = eventTime.currentPlaybackPositionMs
                val positionTimestamp = eventTime.getPositionTimestamp(eventTime.currentPlaybackPositionMs, window)

                setCurrentSession(null, position, positionTimestamp)
            }

            sessions.values.forEach { session ->
                notifyListeners { onSessionDestroyed(session) }
            }
            sessions.clear()
        }
    }

    private companion object {
        private const val TAG = "PlaybackSessionManager"

        private fun EventTime.getPositionTimestamp(currentPositionMs: Long, window: Timeline.Window): Long? {
            if (timeline.isEmpty) {
                return null
            }

            timeline.getWindow(windowIndex, window)

            return if (window.elapsedRealtimeEpochOffsetMs != C.TIME_UNSET) {
                window.windowStartTimeMs + currentPositionMs
            } else {
                null
            }
        }
    }
}
