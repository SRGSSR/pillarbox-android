/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData

internal class QosTracker(
    private val player: ExoPlayer,
) {
    interface EventListener {
        fun onLoadStart(
            player: Player,
            mediaItem: MediaItem,
            mediaLoadData: MediaLoadData,
            loadEventInfo: LoadEventInfo,
        )

        fun onLoadEnd(
            player: Player,
            mediaItem: MediaItem,
            mediaLoadData: MediaLoadData,
            loadEventInfo: LoadEventInfo,
        )

        fun onPlay(
            player: Player,
            mediaItem: MediaItem,
        )

        fun onPause(
            player: Player,
            mediaItem: MediaItem,
        )

        fun onVideoSizeChange(
            player: Player,
            mediaItem: MediaItem,
            videoSize: VideoSize,
        )

        fun onDroppedVideoFrames(
            player: Player,
            mediaItem: MediaItem,
            droppedFrames: Int,
        )

        fun onError(
            player: Player,
            mediaItem: MediaItem,
            error: PlaybackException,
        )
    }

    interface QosListener {
        fun onQosChange(
            mediaItem: MediaItem,
            qosInfo: QosInfo,
        )
    }

    private val inMemoryQosTrackerListener = InMemoryQosTrackerEventListener()
    private val eventListeners = mutableListOf<EventListener>()

    init {
        eventListeners.add(LoggerQosTrackerEventListener())
        eventListeners.add(inMemoryQosTrackerListener)

        player.addAnalyticsListener(AnalyticsPlayerListener())
    }

    fun addQosListener(qosListener: QosListener) {
        inMemoryQosTrackerListener.addQosListener(qosListener)
    }

    fun removeQosListener(qosListener: QosListener) {
        inMemoryQosTrackerListener.removeQosListener(qosListener)
    }

    fun getQosInfo(mediaItem: MediaItem): QosInfo {
        return inMemoryQosTrackerListener.getQosInfo(mediaItem)
    }

    private inner class AnalyticsPlayerListener : AnalyticsListener {
        private val window = Timeline.Window()

        private var playingMediaItem: MediaItem? = null

        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            val mediaItem = getMediaItem(eventTime)

            eventListeners.forEach { listener ->
                listener.onLoadStart(player, mediaItem, mediaLoadData, loadEventInfo)
            }
        }

        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
        ) {
            val mediaItem = getMediaItem(eventTime)

            eventListeners.forEach { listener ->
                listener.onLoadEnd(player, mediaItem, mediaLoadData, loadEventInfo)
            }
        }

        override fun onIsPlayingChanged(
            eventTime: AnalyticsListener.EventTime,
            isPlaying: Boolean,
        ) {
            val currentMediaItem = player.currentMediaItem
            val mediaItem = (if (isPlaying) currentMediaItem else playingMediaItem) ?: return

            eventListeners.forEach { listener ->
                if (isPlaying) {
                    playingMediaItem = currentMediaItem

                    listener.onPlay(player, mediaItem)
                } else {
                    listener.onPause(player, mediaItem)
                }
            }
        }

        override fun onMediaItemTransition(
            eventTime: AnalyticsListener.EventTime,
            mediaItem: MediaItem?,
            @MediaItemTransitionReason reason: Int,
        ) {
            if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                // Notify about the current media item is not playing anymore
                onIsPlayingChanged(eventTime, false)

                playingMediaItem = null

                // Notify about the new media item starting to play
                onIsPlayingChanged(eventTime, true)
            }
        }

        override fun onVideoSizeChanged(
            eventTime: AnalyticsListener.EventTime,
            videoSize: VideoSize,
        ) {
            val mediaItem = getMediaItem(eventTime)

            eventListeners.forEach { listener ->
                listener.onVideoSizeChange(player, mediaItem, videoSize)
            }
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long,
        ) {
            val mediaItem = getMediaItem(eventTime)

            eventListeners.forEach { listener ->
                listener.onDroppedVideoFrames(
                    player = player,
                    mediaItem = mediaItem,
                    droppedFrames = droppedFrames,
                )
            }
        }

        override fun onPlayerError(
            eventTime: AnalyticsListener.EventTime,
            error: PlaybackException,
        ) {
            val mediaItem = getMediaItem(eventTime)

            eventListeners.forEach { listener ->
                listener.onError(player, mediaItem, error)
            }
        }

        private fun getMediaItem(eventTime: AnalyticsListener.EventTime): MediaItem {
            return eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
        }
    }
}
