/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.C
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import kotlin.time.Duration.Companion.milliseconds

internal class StartupTimesTracker : AnalyticsListener, QoSEventsDispatcher.Listener {
    private val loadingSessions = mutableSetOf<String>()
    private val periodUidToSessionId = mutableMapOf<Any, String>()
    private val currentSessionToMediaStart = mutableMapOf<String, Long>()
    private val qosSessionsTimings = mutableMapOf<String, QoSSessionTimings>()
    private val window = Timeline.Window()

    fun consumeStartupTimes(sessionId: String): QoSSessionTimings? {
        if (loadingSessions.remove(sessionId)) {
            val sessionTimings = checkNotNull(qosSessionsTimings.remove(sessionId))
            val currentSessionToMediaStart = currentSessionToMediaStart.remove(sessionId)

            return if (currentSessionToMediaStart != null) {
                sessionTimings.copy(currentToStart = (System.currentTimeMillis() - currentSessionToMediaStart).milliseconds)
            } else {
                sessionTimings
            }
        }

        return null
    }

    override fun onSessionCreated(session: QoSEventsDispatcher.Session) {
        loadingSessions.add(session.sessionId)
        periodUidToSessionId[session.periodUid] = session.sessionId
        qosSessionsTimings[session.sessionId] = QoSSessionTimings.Zero
    }

    override fun onCurrentSession(session: QoSEventsDispatcher.Session) {
        currentSessionToMediaStart[session.sessionId] = System.currentTimeMillis()
    }

    override fun onSessionFinished(session: QoSEventsDispatcher.Session) {
        loadingSessions.remove(session.sessionId)
        periodUidToSessionId.remove(session.periodUid)
        qosSessionsTimings.remove(session.sessionId)
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        val sessionId = getSessionId(eventTime)
        if (sessionId == null || sessionId !in loadingSessions || sessionId !in qosSessionsTimings) {
            return
        }

        val qosSessionTimings = qosSessionsTimings.getValue(sessionId)
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds

        qosSessionsTimings[sessionId] = when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> qosSessionTimings.copy(drm = qosSessionTimings.drm + loadDuration)
            C.DATA_TYPE_MANIFEST, C.DATA_TYPE_MEDIA -> qosSessionTimings.copy(mediaSource = qosSessionTimings.mediaSource + loadDuration)
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> qosSessionTimings.copy(asset = qosSessionTimings.asset + loadDuration)
            else -> qosSessionTimings
        }
    }

    private fun getSessionId(eventTime: AnalyticsListener.EventTime): String? {
        val timeline = eventTime.timeline
        if (timeline.isEmpty) {
            return null
        }

        val firstPeriodIndex = timeline.getWindow(eventTime.windowIndex, window).firstPeriodIndex
        val periodUid = timeline.getUidOfPeriod(firstPeriodIndex)
        return periodUidToSessionId[periodUid]
    }
}
