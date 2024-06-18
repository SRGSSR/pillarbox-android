/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
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
 * - Session are created when player do something with a MediaItem.
 * - Session become current if the media item associated with session is the current.
 * - Session finish when current session become no more current or session is removed from player
 */
class PlaybackSessionManager : AnalyticsListener {

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

    /**
     * Listener
     */
    var listener: Listener? = null

    /**
     * Current session
     */
    var currentSession: Session? = null
        private set(value) {
            if (field != value) {
                field?.let {
                    listener?.onSessionFinished(it)
                    sessions.remove(it.sessionId)
                }
                field = value
                field?.let {
                    listener?.onCurrentSession(it)
                }
            }
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
     * Get or create session from a [MediaItem]
     *
     * @param mediaItem The [MediaItem].
     * @return a [Session] associated with mediaItem.
     */
    fun getOrCreateSession(mediaItem: MediaItem): Session {
        val session = sessions.firstNotNullOfOrNull { if (it.value.mediaItem.isTheSame(mediaItem)) it.value else null }
        if (session == null) {
            val newSession = Session(mediaItem)
            sessions[newSession.sessionId] = newSession
            listener?.onSessionCreated(newSession)
            if (currentSession == null) {
                currentSession = newSession
            }
            return newSession
        }
        return session
    }

    /**
     * On position discontinuity
     *
     * Change current session if needed
     *
     * @param eventTime The event time after the discountinuity
     * @param oldPosition
     * @param newPosition
     * @param reason
     */
    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(eventTime, oldPosition, newPosition, reason)
        val oldItemIndex = oldPosition.mediaItemIndex
        val newItemIndex = newPosition.mediaItemIndex
        Log.d(TAG, "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)}")
        if (oldItemIndex != newItemIndex && !eventTime.timeline.isEmpty) {
            val newSession = getOrCreateSession(eventTime.timeline.getWindow(newItemIndex, Timeline.Window()).mediaItem)
            currentSession = newSession
        }
    }

    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(
            TAG,
            "onMediaItemTransition reason = ${StringUtil.mediaItemTransitionReasonString(reason)} ${mediaItem?.mediaMetadata?.title}"
        )
        currentSession = mediaItem?.let { getOrCreateSession(it) }
    }

    /**
     * On timeline changed
     * - Clear session if timeline become empty
     * - Finish session that are no more in the timeline
     * - Create session for item added to timeline
     */
    @Suppress("NestedBlockDepth")
    override fun onTimelineChanged(eventTime: EventTime, reason: Int) {
        DebugLogger.debug(TAG, "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)} ${eventTime.getMediaItem().mediaMetadata.title}")
        if (eventTime.timeline.isEmpty) {
            finishAllSession()
        } else {
            val timeline = eventTime.timeline
            val window = Timeline.Window()
            val listNewItems = ArrayList<MediaItem>()
            for (i in 0 until timeline.windowCount) {
                val mediaItem = timeline.getWindow(i, window).mediaItem
                listNewItems.add(mediaItem)
            }
            val sessions = HashSet(sessions.values)
            for (session in sessions) {
                val matchingItem = listNewItems.firstOrNull { it.isTheSame(session.mediaItem) }
                if (matchingItem == null) {
                    if (session == currentSession) currentSession = null
                    else {
                        listener?.onSessionFinished(session)
                        this.sessions.remove(session.sessionId)
                    }
                }
            }
        }
    }

    override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        // Log.d(TAG, "onLoadStarted ${eventTime.getMediaItem().mediaMetadata.title}")
        if (eventTime.timeline.isEmpty) return
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
            listener?.onSessionFinished(session)
        }
        sessions.clear()
    }

    companion object {

        private const val TAG = "SessionManager"

        private fun EventTime.getMediaItem(): MediaItem {
            if (timeline.isEmpty) return MediaItem.EMPTY
            return timeline.getWindow(windowIndex, Timeline.Window()).mediaItem
        }

        private fun MediaItem.isTheSame(mediaItem: MediaItem): Boolean {
            return mediaId == mediaItem.mediaId && localConfiguration?.uri == mediaItem.localConfiguration?.uri
        }
    }
}
