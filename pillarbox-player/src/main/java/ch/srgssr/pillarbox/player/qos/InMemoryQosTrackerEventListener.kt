/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class InMemoryQosTrackerEventListener(
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : QosTracker.EventListener {
    private val qosInfo = mutableMapOf<String, QosInfo>()
    private val qosListeners = mutableListOf<QosTracker.QosListener>()

    private var lastPlayTimeMark: TimeMark? = null

    fun addQosListener(qosListener: QosTracker.QosListener) {
        qosListeners.add(qosListener)
    }

    fun removeQosListener(qosListener: QosTracker.QosListener) {
        qosListeners.remove(qosListener)
    }

    fun getQosInfo(mediaItem: MediaItem): QosInfo {
        return qosInfo.getOrPut(mediaItem.mediaId) { QosInfo.Empty }
    }

    override fun onLoadStart(
        player: Player,
        mediaItem: MediaItem,
        mediaLoadData: MediaLoadData,
        loadEventInfo: LoadEventInfo,
    ) = Unit

    override fun onLoadEnd(
        player: Player,
        mediaItem: MediaItem,
        mediaLoadData: MediaLoadData,
        loadEventInfo: LoadEventInfo,
    ) {
        updateQosLoadTime(mediaItem) { loadTime ->
            val loadDuration = loadEventInfo.loadDurationMs.milliseconds

            when (val dataType = mediaLoadData.dataType) {
                C.DATA_TYPE_AD -> loadTime.copy(ad = loadTime.ad + loadDuration)
                C.DATA_TYPE_DRM -> loadTime.copy(drm = loadTime.drm + loadDuration)
                C.DATA_TYPE_MANIFEST -> loadTime.copy(manifest = loadTime.manifest + loadDuration)
                C.DATA_TYPE_MEDIA -> loadTime.copy(media = loadTime.media + loadDuration)
                C.DATA_TYPE_MEDIA_INITIALIZATION -> loadTime.copy(mediaInitialization = loadTime.mediaInitialization + loadDuration)
                C.DATA_TYPE_MEDIA_PROGRESSIVE_LIVE -> loadTime.copy(mediaProgressiveLive = loadTime.mediaProgressiveLive + loadDuration)
                C.DATA_TYPE_TIME_SYNCHRONIZATION -> loadTime.copy(timeSynchronization = loadTime.timeSynchronization + loadDuration)
                C.DATA_TYPE_UNKNOWN -> loadTime.copy(unknown = loadTime.unknown + loadDuration)
                else -> {
                    val customLoadTimes = loadTime.custom.toMutableMap()
                    customLoadTimes[dataType] = customLoadTimes.getOrPut(dataType) { Duration.ZERO } + loadDuration

                    loadTime.copy(custom = customLoadTimes)
                }
            }
        }
    }

    override fun onPlay(
        player: Player,
        mediaItem: MediaItem,
    ) {
        lastPlayTimeMark = timeSource.markNow()
    }

    override fun onPause(
        player: Player,
        mediaItem: MediaItem,
    ) {
        lastPlayTimeMark?.let { timeMark ->
            updateQosInfo(mediaItem) { qosInfo ->
                qosInfo.copy(playTime = qosInfo.playTime + timeMark.elapsedNow())
            }

            lastPlayTimeMark = null
        }
    }

    override fun onVideoSizeChange(
        player: Player,
        mediaItem: MediaItem,
        videoSize: VideoSize,
    ) {
        updateQosInfo(mediaItem) { qosInfo ->
            qosInfo.copy(videoSize = videoSize)
        }
    }

    override fun onDroppedVideoFrames(
        player: Player,
        mediaItem: MediaItem,
        droppedFrames: Int,
    ) {
        updateQosInfo(mediaItem) { qosInfo ->
            qosInfo.copy(droppedFrames = qosInfo.droppedFrames + droppedFrames)
        }
    }

    override fun onError(
        player: Player,
        mediaItem: MediaItem,
        error: PlaybackException,
    ) {
        updateQosInfo(mediaItem) { qosInfo ->
            qosInfo.copy(errors = qosInfo.errors + 1)
        }
    }

    private inline fun updateQosInfo(
        mediaItem: MediaItem,
        update: (qosInfo: QosInfo) -> QosInfo,
    ) {
        val mediaId = mediaItem.mediaId
        val originalQosInfo = qosInfo.getOrPut(mediaId) { QosInfo.Empty }
        val updatedQosInfo = update(originalQosInfo)

        qosInfo[mediaId] = updatedQosInfo

        qosListeners.forEach { listener ->
            listener.onQosChange(mediaItem, updatedQosInfo)
        }
    }

    private inline fun updateQosLoadTime(
        mediaItem: MediaItem,
        update: (loadTime: LoadTime) -> LoadTime,
    ) {
        updateQosInfo(mediaItem) { qosInfo ->
            qosInfo.copy(loadTime = update(qosInfo.loadTime))
        }
    }
}
