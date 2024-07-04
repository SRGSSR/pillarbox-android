/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import kotlin.time.Duration.Companion.milliseconds

internal class QoSSessionAnalyticsListener(
    private val context: Context,
    private val onQoSSessionReady: (qosSession: QoSSession) -> Unit,
) : AnalyticsListener {
    private val loadingSessions = mutableSetOf<String>()
    private val periodUidToSessionId = mutableMapOf<Any, String>()
    private val currentSessionToMediaStart = mutableMapOf<String, Long>()
    private val qosSessions = mutableMapOf<String, QoSSession>()
    private val window = Timeline.Window()

    fun onSessionCreated(session: QoSEventsDispatcher.Session) {
        loadingSessions.add(session.sessionId)
        periodUidToSessionId[session.periodUid] = session.sessionId
        qosSessions[session.sessionId] = QoSSession(
            context = context,
            mediaId = session.mediaItem.mediaId,
            mediaSource = session.mediaItem.localConfiguration?.uri?.toString().orEmpty(),
        )
    }

    fun onCurrentSession(session: QoSEventsDispatcher.Session) {
        currentSessionToMediaStart[session.sessionId] = System.currentTimeMillis()
    }

    fun onSessionFinished(session: QoSEventsDispatcher.Session) {
        loadingSessions.remove(session.sessionId)
        periodUidToSessionId.remove(session.periodUid)
        qosSessions.remove(session.sessionId)
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        val sessionId = getSessionId(eventTime)
        if (sessionId == null || sessionId !in loadingSessions || sessionId !in qosSessions) {
            return
        }

        val qosSession = qosSessions.getValue(sessionId)
        val initialTimings = qosSession.timings
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds

        val timings = when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> initialTimings.copy(drm = initialTimings.drm + loadDuration)
            C.DATA_TYPE_MANIFEST, C.DATA_TYPE_MEDIA -> initialTimings.copy(mediaSource = initialTimings.mediaSource + loadDuration)
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> initialTimings.copy(asset = initialTimings.asset + loadDuration)
            else -> initialTimings
        }

        qosSessions[sessionId] = qosSession.copy(timings = timings)
    }

    override fun onAudioPositionAdvancing(
        eventTime: AnalyticsListener.EventTime,
        playoutStartSystemTimeMs: Long,
    ) {
        notifyQoSSessionReady(eventTime)
    }

    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        notifyQoSSessionReady(eventTime)
    }

    private fun notifyQoSSessionReady(eventTime: AnalyticsListener.EventTime) {
        val sessionId = getSessionId(eventTime) ?: return

        if (loadingSessions.remove(sessionId)) {
            qosSessions[sessionId]?.let {
                val qosSession = if (sessionId in currentSessionToMediaStart) {
                    it.copy(
                        timings = it.timings.copy(
                            currentToStart = (System.currentTimeMillis() - currentSessionToMediaStart.getValue(sessionId)).milliseconds,
                        ),
                    )
                } else {
                    it
                }

                currentSessionToMediaStart.remove(sessionId)
                onQoSSessionReady(qosSession)
            }
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
