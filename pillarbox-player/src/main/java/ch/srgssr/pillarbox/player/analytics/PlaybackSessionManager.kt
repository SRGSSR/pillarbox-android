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
import androidx.media3.common.Timeline.EMPTY
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

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
     * - A session is destroyed when
     *      - It is no longer the current session.
     *      - It is removed from the player.
     *      - The player is released.
     *
     * @property periodUid The period id from [Timeline.getUidOfPeriod][androidx.media3.common.Timeline.getUidOfPeriod] for [mediaItem].
     * @property window The last known [Timeline.Window].
     */
    class Session(
        val periodUid: Any,
        val window: Window
    ) {
        /**
         * Unique session id.
         */
        val sessionId = UUID.randomUUID().toString()

        /**
         * Media item
         */
        val mediaItem: MediaItem = window.mediaItem

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Session

            if (periodUid != other.periodUid) return false
            if (sessionId != other.sessionId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = periodUid.hashCode()
            result = 31 * result + sessionId.hashCode()
            return result
        }

        override fun toString(): String {
            return "Session(periodUid=$periodUid, sessionId='$sessionId', mediaItemId=${mediaItem.mediaId})"
        }
    }

    /**
     * Session info
     *
     * @property session The [Session]
     * @property position The position in milliseconds when a session change occurs.
     */
    data class SessionInfo(
        val session: Session,
        val position: Long
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
    private val window = Window()
    private var currentTimeline: Timeline = EMPTY
    private var lastTimeline: Timeline = currentTimeline
    private var _currentSession: Session? = null

    private fun setCurrentSession(newSession: SessionInfo?, oldPosition: Long) {
        if (_currentSession == newSession?.session) {
            return
        }
        val oldSession = _currentSession
        _currentSession = newSession?.session
        notifyListeners {
            onCurrentSessionChanged(oldSession?.let { SessionInfo(it, oldPosition) }, newSession)
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
        currentTimeline = player.currentTimeline
        lastTimeline = currentTimeline
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
            DebugLogger.debug(
                TAG,
                "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)} " +
                    "${oldPosition.mediaItemIndex} -> ${newPosition.mediaItemIndex} " +
                    "${oldPosition.positionMs.milliseconds} -> ${newPosition.positionMs.milliseconds} " +
                    "${lastTimeline.windowCount} ${currentTimeline.windowCount} $lastTimeline $currentTimeline"
            )
            if (reason == Player.DISCONTINUITY_REASON_REMOVE) {
                val newSession = newPosition.periodUid
                    ?.let(::getSessionFromPeriodUid)
                    ?.let { SessionInfo(it, newPosition.positionMs) }
                setCurrentSession(newSession, oldPosition.positionMs)
            } else if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex && !eventTime.timeline.isEmpty) {
                val newSession = checkNotNull(getOrCreateSession(eventTime)) // Return null only if timeline is empty
                setCurrentSession(SessionInfo(newSession, newPosition.positionMs), oldPosition.positionMs)
            }
        }

        private fun updateSession(timeline: Timeline) {
            for (session in sessions.values) {
                val windowIndex = timeline.getIndexOfPeriod(session.periodUid)
                if (windowIndex != C.INDEX_UNSET) {
                    timeline.getWindow(windowIndex, session.window)
                }
            }
        }

        override fun onTimelineChanged(
            eventTime: EventTime,
            @TimelineChangeReason reason: Int,
        ) {
            DebugLogger.debug(TAG, "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)}")
            val timeline = eventTime.timeline
            lastTimeline = currentTimeline
            currentTimeline = timeline
            when (reason) {
                Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> {
                    updateSession(eventTime.timeline)
                }

                Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> {
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
            finishAllSessions(eventTime.eventPlaybackPositionMs)
            listeners.clear()
        }

        override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
            DebugLogger.debug(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)}")
            when (state) {
                Player.STATE_ENDED -> setCurrentSession(
                    null,
                    eventTime.currentPlaybackPositionMs
                )

                Player.STATE_IDLE -> Unit
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
                val newSession = Session(periodUid, eventTime.timeline.getWindow(eventTime.windowIndex, Window()))
                sessions[periodUid] = newSession
                notifyListeners { onSessionCreated(newSession) }

                if (_currentSession == null) {
                    val position = eventTime.currentPlaybackPositionMs
                    setCurrentSession(SessionInfo(newSession, position), C.TIME_UNSET)
                }

                session = newSession
            }

            return session
        }

        private fun finishAllSessions(position: Long) {
            DebugLogger.debug(TAG, "Finish all sessions @ ${position.milliseconds} $_currentSession")
            _currentSession?.let {
                setCurrentSession(null, position)
            }

            sessions.values.forEach { session ->
                notifyListeners { onSessionDestroyed(session) }
            }
            sessions.clear()
        }
    }

    private companion object {
        private const val TAG = "PlaybackSessionManager"
    }
}
