/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.TimelineChangeReason
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
 *
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
     * @property mediaItem The [MediaItem] linked to the session.
     */
    class Session(val mediaItem: MediaItem) {
        /**
         * Unique Session Id
         */
        val sessionId = UUID.randomUUID().toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Session

            return sessionId == other.sessionId
        }

        override fun hashCode(): Int {
            return sessionId.hashCode()
        }
    }

    private val sessions = HashMap<String, Session>()
    private val window = Window()

    /**
     * Current session
     */
    var currentSession: Session? = null
        private set(value) {
            if (field != value) {
                field?.let {
                    listener.onSessionFinished(it)
                    sessions.remove(it.sessionId)
                }
                field = value
                field?.let {
                    listener.onCurrentSession(it)
                }
            }
        }

    /**
     * Get or create a session from a [MediaItem].
     *
     * @param mediaItem The [MediaItem].
     * @return A [Session] associated with `mediaItem`.
     */
    fun getOrCreateSession(mediaItem: MediaItem): Session {
        val session = sessions.values.firstOrNull { it.mediaItem.isTheSame(mediaItem) }
        if (session == null) {
            val newSession = Session(mediaItem)
            sessions[newSession.sessionId] = newSession
            listener.onSessionCreated(newSession)
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
            val newSession = getOrCreateSession(eventTime.timeline.getWindow(newItemIndex, window).mediaItem)
            currentSession = newSession
        }
    }

    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(
            TAG,
            "onMediaItemTransition reason = ${StringUtil.mediaItemTransitionReasonString(reason)} ${mediaItem?.mediaMetadata?.title}"
        )

        mediaItem?.let { mediaItem ->
            sessions.values.firstOrNull { it.mediaItem.isTheSame(mediaItem) }
        }?.let { session ->
            currentSession = session
        }
    }

    override fun onTimelineChanged(eventTime: EventTime, @TimelineChangeReason reason: Int) {
        DebugLogger.debug(TAG, "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)} ${eventTime.getMediaItem().mediaMetadata.title}")
        if (eventTime.timeline.isEmpty) {
            finishAllSession()
        } else if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            refreshSessions(eventTime)
        } else if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            currentSession = getOrCreateSession(eventTime.getMediaItem())
        }
    }

    private fun refreshSessions(eventTime: EventTime) {
        val timeline = eventTime.timeline
        val sessions = sessions.values.toSet()
        val mediaItems = (0 until timeline.windowCount).map { windowIndex ->
            timeline.getWindow(windowIndex, window).mediaItem
        }

        sessions.forEach { session ->
            val hasMediaItem = mediaItems.any { it.isTheSame(session.mediaItem) }
            if (!hasMediaItem) {
                if (session == currentSession) {
                    currentSession = null
                } else {
                    listener.onSessionFinished(session)
                    this.sessions.remove(session.sessionId)
                }
            }
        }
    }

    override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        val mediaItem = eventTime.getMediaItem()
        if (mediaItem != MediaItem.EMPTY) {
            getOrCreateSession(mediaItem)
        }
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

        private fun MediaItem.isTheSame(mediaItem: MediaItem): Boolean {
            return mediaId == mediaItem.mediaId && localConfiguration?.uri == mediaItem.localConfiguration?.uri
        }
    }
}
