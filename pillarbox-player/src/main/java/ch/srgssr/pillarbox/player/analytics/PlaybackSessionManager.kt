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
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.TimelineChangeReason
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
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

    private val analyticsListener = SessionManagerAnalyticsListener()
    private val listeners = mutableSetOf<Listener>()
    private val sessions = mutableMapOf<Any, Session>()
    private val window = Timeline.Window()

    private var currentSession: Session? = null
        set(value) {
            if (field != value) {
                field?.let { session ->
                    notifyListeners { onSessionFinished(session) }
                    sessions.remove(session.periodUid)
                }
                field = value
                field?.let { session ->
                    notifyListeners { onCurrentSession(session) }
                }
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
        return currentSession
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
    fun getSessionFromEventTime(eventTime: AnalyticsListener.EventTime): Session? {
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
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @DiscontinuityReason reason: Int
        ) {
            val oldItemIndex = oldPosition.mediaItemIndex
            val newItemIndex = newPosition.mediaItemIndex

            DebugLogger.debug(TAG, "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)}")

            if (oldItemIndex != newItemIndex && !eventTime.timeline.isEmpty) {
                currentSession = getOrCreateSession(eventTime)
            }
        }

        override fun onMediaItemTransition(
            eventTime: AnalyticsListener.EventTime,
            mediaItem: MediaItem?,
            @MediaItemTransitionReason reason: Int,
        ) {
            DebugLogger.debug(
                TAG,
                "onMediaItemTransition reason = ${StringUtil.mediaItemTransitionReasonString(reason)} ${mediaItem?.mediaMetadata?.title}",
            )

            currentSession = mediaItem?.let { getOrCreateSession(eventTime) }
        }

        override fun onTimelineChanged(
            eventTime: AnalyticsListener.EventTime,
            @TimelineChangeReason reason: Int,
        ) {
            val mediaItem = if (eventTime.timeline.isEmpty) {
                MediaItem.EMPTY
            } else {
                eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
            }

            DebugLogger.debug(TAG, "onTimelineChanged reason = ${StringUtil.timelineChangeReasonString(reason)} ${mediaItem.mediaMetadata.title}")

            val timeline = eventTime.timeline
            if (timeline.isEmpty) {
                finishAllSessions()
                return
            }

            // Finish sessions that are no longer in the timeline
            val currentSessions = sessions.values.toSet()
            currentSessions.forEach { session ->
                val periodUid = session.periodUid
                val periodIndex = timeline.getIndexOfPeriod(periodUid)
                if (periodIndex == C.INDEX_UNSET) {
                    if (session == currentSession) {
                        currentSession = null
                    } else {
                        notifyListeners { onSessionFinished(session) }
                        sessions.remove(session.periodUid)
                    }
                }
            }
        }

        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            getOrCreateSession(eventTime)
        }

        override fun onPlayerError(
            eventTime: AnalyticsListener.EventTime,
            error: PlaybackException,
        ) {
            DebugLogger.debug(TAG, "onPlayerError", error)
            getOrCreateSession(eventTime)
        }
        override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
            DebugLogger.debug(TAG, "onPlayerReleased")
            finishAllSessions()
            listeners.clear()
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            DebugLogger.debug(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)}")
            if (state == Player.STATE_IDLE) {
                finishAllSessions()
            } else {
                getOrCreateSession(eventTime)
            }
        }

        private fun getOrCreateSession(eventTime: AnalyticsListener.EventTime): Session? {
            if (eventTime.timeline.isEmpty) {
                return null
            }
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            val periodUid = eventTime.timeline.getUidOfPeriod(window.firstPeriodIndex)
            var session = getSessionFromPeriodUid(periodUid)
            if (session == null) {
                val newSession = Session(periodUid, window.mediaItem)
                sessions[periodUid] = newSession
                notifyListeners { onSessionCreated(newSession) }

                if (currentSession == null) {
                    currentSession = newSession
                }

                session = newSession
            }

            return session
        }

        private fun finishAllSessions() {
            currentSession = null

            sessions.values.forEach { session ->
                notifyListeners { onSessionFinished(session) }
            }
            sessions.clear()
        }
    }

    private companion object {
        private const val TAG = "PlaybackSessionManager"
    }
}
