/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import kotlin.time.Duration.Companion.milliseconds

internal class QoSSessionAnalyticsListener(
    private val context: Context,
    private val onQoSSessionReady: (qosSession: QoSSession) -> Unit,
) : AnalyticsListener {
    private val loadingSessions = mutableSetOf<String>()
    private val mediaToSessionId = mutableMapOf<MediaItem, String>()
    private val qosSessions = mutableMapOf<String, QoSSession>()
    private val window = Timeline.Window()

    fun onSessionCreated(session: PlaybackSessionManager.Session) {
        loadingSessions.add(session.sessionId)
        mediaToSessionId[session.mediaItem] = session.sessionId
        qosSessions[session.sessionId] = QoSSession(
            context = context,
            mediaId = session.mediaItem.mediaId,
            mediaSource = session.mediaItem.localConfiguration?.uri?.toString().orEmpty(),
        )
    }

    fun onCurrentSession(session: PlaybackSessionManager.Session) {
        if (loadingSessions.remove(session.sessionId)) {
            qosSessions[session.sessionId]?.let(onQoSSessionReady)
        }
    }

    fun onSessionFinished(session: PlaybackSessionManager.Session) {
        loadingSessions.remove(session.sessionId)
        mediaToSessionId.remove(session.mediaItem)
        qosSessions.remove(session.sessionId)
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        val mediaItem = getMediaItem(eventTime)
        val sessionId = mediaToSessionId[mediaItem]
        if (sessionId == null || sessionId !in loadingSessions || sessionId !in qosSessions) {
            return
        }

        val qosSession = qosSessions.getValue(sessionId)
        val initialTimings = qosSession.timings
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds

        val timings = when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> initialTimings.copy(drm = initialTimings.drm + loadDuration)
            C.DATA_TYPE_MEDIA -> initialTimings.copy(mediaSource = initialTimings.mediaSource + loadDuration)
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> initialTimings.copy(asset = initialTimings.asset + loadDuration)
            else -> initialTimings
        }

        qosSessions[sessionId] = qosSession.copy(timings = timings)
    }

    private fun getMediaItem(eventTime: AnalyticsListener.EventTime): MediaItem? {
        return if (eventTime.timeline.isEmpty) {
            null
        } else {
            eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
        }
    }
}
