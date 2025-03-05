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
 * Manages playback sessions, representing interactions with individual [MediaItem]s.
 */
class PlaybackSessionManager : AnalyticsListener {
    /**
     * Represents a playback session associated with a [MediaItem] in a [Timeline].
     *
     * - A session is linked to the period inside the timeline, see [Timeline.getUidOfPeriod].
     * - A session is created when the player interacts with a [MediaItem].
     * - A session is considered the "current" session if its [mediaItem] is the [current `MediaItem`][Player.getCurrentMediaItem].
     * - A session is destroyed when:
     *     - It's no longer the current session (e.g., switching media items).
     *     - Its [mediaItem] is removed from the playlist.
     *     - The player is released.
     *
     * @property periodUid The id of the period in the timeline, obtained from [Timeline.getUidOfPeriod].
     * @property window The last known [Window] associated with this session.
     */
    class Session(
        val periodUid: Any,
        val window: Window
    ) {
        /**
         * Unique identifier for this session.
         */
        val sessionId = UUID.randomUUID().toString()

        /**
         * The [MediaItem] associated with this session.
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
     * Represents information about a session at a specific point in time.
     *
     * @property session The [Session].
     * @property position The position, in milliseconds, within a timeline when this session information is relevant.
     */
    data class SessionInfo(
        val session: Session,
        val position: Long
    )

    /**
     * An interface for receiving notifications about session lifecycle events.
     */
    interface Listener {
        /**
         * Called when a new session is created.
         *
         * @param session The newly created [Session].
         */
        fun onSessionCreated(session: Session) = Unit

        /**
         * Called when a session is destroyed. The session will no longer be the current session.
         *
         * @param session The [Session] that has been destroyed.
         */
        fun onSessionDestroyed(session: Session) = Unit

        /**
         * Called when the current session changes.
         *
         * Immediately following this call, [onSessionDestroyed] will be called with [oldSession] to signal the previous session's termination.
         *
         * @param oldSession The previously active [Session], or `null` if there was none.
         * @param newSession The newly active [Session], or `null` if there is none.
         */
        fun onCurrentSessionChanged(
            oldSession: SessionInfo?,
            newSession: SessionInfo?,
        ) = Unit
    }

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
     * Adds a listener to this session manager.
     *
     * @param listener The listener to be added.
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener from this session manager.
     *
     * @param listener The listener to be removed.
     */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * Returns the current session.
     *
     * @return The current session, or `null` if there is no active session.
     */
    fun getCurrentSession(): Session? {
        return _currentSession
    }

    /**
     * Retrieves a session by its id.
     *
     * @param sessionId The id of the session to retrieve.
     * @return The session identified by [sessionId] if found, `null` otherwise.
     */
    fun getSessionById(sessionId: String): Session? {
        return sessions.values.find { it.sessionId == sessionId }
    }

    /**
     * Retrieves the [Session] associated with a given [EventTime].
     *
     * @param eventTime The [EventTime] representing the event for which to retrieve the session.
     * @return The [Session] associated with the event, or `null` if no session could be found.
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
     * Retrieves the [Session] associated with a given period UID.
     *
     * @param periodUid The unique identifier of the period to retrieve the session for.
     * @return The [Session] associated with the [periodUid], or `null` no session could be found.
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
            Player.STATE_ENDED -> setCurrentSession(null, eventTime.currentPlaybackPositionMs)
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

    private companion object {
        private const val TAG = "PlaybackSessionManager"
    }
}
