/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.TimelineChangeReason
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.qos.QoSEventsDispatcher.Listener
import ch.srgssr.pillarbox.player.qos.QoSEventsDispatcher.Session
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil

/**
 * Pillarbox provided implementation of [QoSEventsDispatcher].
 */
class PillarboxEventsDispatcher : QoSEventsDispatcher {
    private val analyticsListener = EventsDispatcherAnalyticsListener()
    private val listeners = mutableSetOf<Listener>()

    override fun registerPlayer(player: ExoPlayer) {
        player.addAnalyticsListener(analyticsListener)
    }

    override fun unregisterPlayer(player: ExoPlayer) {
        player.removeAnalyticsListener(analyticsListener)
    }

    override fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private inline fun notifyListeners(event: Listener.() -> Unit) {
        listeners.toList()
            .forEach { listener ->
                listener.event()
            }
    }

    private inner class EventsDispatcherAnalyticsListener : AnalyticsListener {
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

        override fun onPositionDiscontinuity(
            eventTime: EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @DiscontinuityReason reason: Int,
        ) {
            val oldItemIndex = oldPosition.mediaItemIndex
            val newItemIndex = newPosition.mediaItemIndex

            DebugLogger.debug(TAG, "onPositionDiscontinuity reason = ${StringUtil.discontinuityReasonString(reason)}")

            if (oldItemIndex != newItemIndex && !eventTime.timeline.isEmpty) {
                currentSession = getOrCreateSession(eventTime)
            }
        }

        override fun onMediaItemTransition(
            eventTime: EventTime,
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
            eventTime: EventTime,
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
            eventTime: EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            getOrCreateSession(eventTime)
        }

        override fun onAudioPositionAdvancing(
            eventTime: EventTime,
            playoutStartSystemTimeMs: Long,
        ) {
            val session = getOrCreateSession(eventTime) ?: return

            notifyListeners { onMediaStart(session) }
        }

        override fun onRenderedFirstFrame(
            eventTime: EventTime,
            output: Any,
            renderTimeMs: Long,
        ) {
            val session = getOrCreateSession(eventTime) ?: return

            notifyListeners { onMediaStart(session) }
        }

        override fun onPlayerReleased(eventTime: EventTime) {
            DebugLogger.debug(TAG, "onPlayerReleased")
            finishAllSessions()
            notifyListeners { onPlayerReleased() }
        }

        private fun getOrCreateSession(eventTime: EventTime): Session? {
            if (eventTime.timeline.isEmpty) {
                return null
            }

            eventTime.timeline.getWindow(eventTime.windowIndex, window)

            val periodUid = eventTime.timeline.getUidOfPeriod(window.firstPeriodIndex)
            var session = sessions[periodUid]
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
        private const val TAG = "PillarboxEventsDispatcher"
    }
}
