/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import kotlin.time.Duration.Companion.milliseconds

internal class QoSSessionAnalyticsListener(private val context: Context) : AnalyticsListener {
    private val loadingSessions = mutableSetOf<String>()
    private val qosSessions = mutableMapOf<String, QoSSession>()
    private val window = Timeline.Window()

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        val mediaItem = getMediaItem(eventTime)
        val mediaId = mediaItem.mediaId

        if (mediaId !in qosSessions) {
            loadingSessions.add(mediaId)
            qosSessions[mediaId] = createQoSSession(mediaItem)
        } else if (mediaId !in loadingSessions) {
            return
        }

        val qosSession = qosSessions.getValue(mediaId)
        val initialTimings = qosSession.timings
        val loadDuration = loadEventInfo.loadDurationMs.milliseconds

        val timings = when (mediaLoadData.dataType) {
            C.DATA_TYPE_DRM -> initialTimings.copy(drm = initialTimings.drm + loadDuration)
            C.DATA_TYPE_MEDIA -> initialTimings.copy(mediaSource = initialTimings.mediaSource + loadDuration)
            PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET -> initialTimings.copy(asset = initialTimings.asset + loadDuration)
            else -> return
        }

        qosSessions[mediaId] = qosSession.copy(timings = timings)
    }

    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        val mediaItem = getMediaItem(eventTime)
        val mediaId = mediaItem.mediaId
        loadingSessions.remove(mediaId)

        // TODO Do something with the sessions
        Log.d("QoSSessionAnalyticsListener", "[$mediaId] ${qosSessions[mediaId]}")
    }

    private fun getMediaItem(eventTime: AnalyticsListener.EventTime): MediaItem {
        return eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
    }

    private fun createQoSSession(mediaItem: MediaItem): QoSSession {
        return QoSSession(
            context = context,
            mediaId = mediaItem.mediaId,
            mediaSource = mediaItem.localConfiguration?.uri?.toString().orEmpty(),
        )
    }
}
