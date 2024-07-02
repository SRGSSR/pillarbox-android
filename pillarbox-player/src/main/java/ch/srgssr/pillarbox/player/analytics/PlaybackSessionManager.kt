/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.TimelineChangeReason
import androidx.media3.common.Timeline
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import java.util.UUID

/**
 * Playback session manager
 * - Session is linked to the period inside the timeline. [Timeline.getUidOfPeriod].
 * - Session is created when the player does something with a [MediaItem].
 * - Session is current if the media item associated with session is the current [MediaItem].
 * - Session is finished when it is no longer the current session or when the session is removed from the player.
 *
 * @param listener The listener attached to the session manager.
 */
class PlaybackSessionManager(
    private val listener: Listener,
) : AnalyticsListener {
    /**
     * Listener
     */
    interface Listener {
        /**
         * On session created
         *
         * @param session
         */
        fun onSessionCreated(session: Session)

        /**
         * On session finished
         *
         * @param session
         */
        fun onSessionFinished(session: Session)

        /**
         * On current session
         *
         * @param session
         */
        fun onCurrentSession(session: Session)
    }

    /**
     * Session
     *
     * @property periodUid The periodUid from [Timeline.getUidOfPeriod] for [mediaItem].
     * @property mediaItem The [MediaItem] linked to the session.
     */
    data class Session(
        val periodUid: Any,
        val mediaItem: MediaItem,
    ) {
        /**
         * Unique Session Id
         */
        val sessionId = UUID.randomUUID().toString()
    }

    private val sessions = HashMap<Any, Session>()
    private val window = Window()

    /**
     * Current session
     */
    var currentSession: Session? = null
        private set(value) {
            if (field != value) {
                field?.let {
                    listener.onSessionFinished(it)
                    sessions.remove(it.periodUid)
                }
                field = value
                field?.let {
                    listener.onCurrentSession(it)
                }
            }
        }

    /**
     * Get session from [periodUid]
     *
     * @param periodUid The period unique id [Timeline.getUidOfPeriod].
     * @return null if session doesn't exist.
     */
    fun getSessionFromPeriodUid(periodUid: Any): Session? {
        return sessions[periodUid]
    }

    /**
     * Get session from event time
     *
     * @param eventTime The [EventTime].
     * @return null if session doesn't exist.
     */
    fun getSessionFromEventTime(eventTime: EventTime): Session? {
        val windowIndex = eventTime.windowIndex
        eventTime.timeline.getWindow(windowIndex, window)
        val periodUid = eventTime.timeline.getUidOfPeriod(window.firstPeriodIndex)
        return sessions[periodUid]
    }

    /**
     * Get or create a session from a [EventTime].
     *
     * @param eventTime The [EventTime].
     * @return A [Session] associated with `eventTime`.
     */
    fun getOrCreateSession(eventTime: EventTime): Session {
        val windowIndex = eventTime.windowIndex
        eventTime.timeline.getWindow(windowIndex, window)
        val periodUid = eventTime.timeline.getUidOfPeriod(window.firstPeriodIndex)
        val session = sessions[periodUid]
        if (session == null) {
            val newSession = Session(periodUid, eventTime.getMediaItem())
            sessions[periodUid] = newSession
            listener.onSessionCreated(newSession)
            if (currentSession == null) {
                currentSession = newSession
            }
            return newSession
        }
        return session
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        val oldItemIndex = oldPosition.mediaItemIndex
        val newItemIndex = newPosition.mediaItemIndex
        DebugLogger.debug(TAG, "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)}")
        if (oldItemIndex != newItemIndex && !eventTime.timeline.isEmpty) {
            val newSession = getOrCreateSession(eventTime)
            currentSession = newSession
        }
    }

    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(
            TAG,
            "onMediaItemTransition reason = ${StringUtil.mediaItemTransitionReasonString(reason)} ${mediaItem?.mediaMetadata?.title}"
        )
        currentSession = if (mediaItem == null) {
            null
        } else {
            getOrCreateSession(eventTime)
        }
    }

    override fun onTimelineChanged(eventTime: EventTime, @TimelineChangeReason reason: Int) {
        DebugLogger.debug(TAG, "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)} ${eventTime.getMediaItem().mediaMetadata.title}")
        if (eventTime.timeline.isEmpty) {
            finishAllSession()
            return
        }
        // Finish sessions that are no more in the timeline.
        val timeline = eventTime.timeline
        val currentSessions = HashSet(sessions.values)
        for (session in currentSessions) {
            val periodUid = session.periodUid
            val periodIndex = timeline.getIndexOfPeriod(periodUid)
            if (periodIndex == C.INDEX_UNSET) {
                if (session == currentSession) {
                    currentSession = null
                } else {
                    listener.onSessionFinished(session)
                    this.sessions.remove(session.periodUid)
                }
            }
        }
    }

    override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        getOrCreateSession(eventTime)
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onPlayerReleased")
        finishAllSession()
    }

    private fun finishAllSession() {
        currentSession = null
        for (session in sessions.values) {
            listener.onSessionFinished(session)
        }
        sessions.clear()
    }

    companion object {

        private const val TAG = "SessionManager"

        private fun EventTime.getMediaItem(): MediaItem {
            if (timeline.isEmpty) return MediaItem.EMPTY
            return timeline.getWindow(windowIndex, Window()).mediaItem
        }
    }
}
