/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class QoSSessionAnalyticsListener(
    private val context: Context,
    private val onQoSSessionReady: (qosSession: QoSSession) -> Unit,
) : AnalyticsListener {
    private val loadingSessions = mutableSetOf<String>()
    private val qosSessions = mutableMapOf<String, QoSSession>()
    private val window = Timeline.Window()

    @Suppress("ReturnCount")
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        val mediaItem = getMediaItem(eventTime) ?: return
        val sessionId = getSessionId(mediaItem)

        if (sessionId !in qosSessions) {
            loadingSessions.add(sessionId)
            qosSessions[sessionId] = createQoSSession(mediaItem)
        } else if (sessionId !in loadingSessions) {
            return
        }

        val qosSession = qosSessions.getValue(sessionId)
        val initialTimings = qosSession.timings
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds

        val timings = when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> initialTimings.copy(drm = initialTimings.drm + loadDuration)
            C.DATA_TYPE_MEDIA -> initialTimings.copy(mediaSource = initialTimings.mediaSource + loadDuration)
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> initialTimings.copy(asset = initialTimings.asset + loadDuration)
            else -> return
        }

        qosSessions[sessionId] = qosSession.copy(timings = timings)
    }

    override fun onTracksChanged(
        eventTime: AnalyticsListener.EventTime,
        tracks: Tracks,
    ) {
        val mediaItem = getMediaItem(eventTime) ?: return
        val sessionId = getSessionId(mediaItem)

        if (loadingSessions.remove(sessionId)) {
            qosSessions[sessionId]?.let(onQoSSessionReady)
        }
    }

    private fun getSessionId(mediaItem: MediaItem): String {
        val mediaId = mediaItem.mediaId
        val mediaUrl = mediaItem.localConfiguration?.uri?.toString().orEmpty()
        val name = (mediaId + mediaUrl).toByteArray()

        return UUID.nameUUIDFromBytes(name).toString()
    }

    private fun getMediaItem(eventTime: AnalyticsListener.EventTime): MediaItem? {
        return if (eventTime.timeline.isEmpty) {
            null
        } else {
            eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
        }
    }

    private fun createQoSSession(mediaItem: MediaItem): QoSSession {
        return QoSSession(
            context = context,
            mediaId = mediaItem.mediaId,
            mediaSource = mediaItem.localConfiguration?.uri?.toString().orEmpty(),
        )
    }
}
