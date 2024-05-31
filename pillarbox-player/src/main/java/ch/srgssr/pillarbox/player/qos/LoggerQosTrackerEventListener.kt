/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.utils.DebugLogger

internal class LoggerQosTrackerEventListener : QosTracker.EventListener {
    override fun onLoadStart(
        player: Player,
        mediaItem: MediaItem,
        mediaLoadData: MediaLoadData,
        loadEventInfo: LoadEventInfo,
    ) {
        log(mediaItem, "onLoad: ${mediaItem.localConfiguration?.uri}, mediaLoadData=$mediaLoadData, loadEventInfo=$loadEventInfo")
    }

    override fun onLoadEnd(
        player: Player,
        mediaItem: MediaItem,
        mediaLoadData: MediaLoadData,
        loadEventInfo: LoadEventInfo,
    ) {
        log(mediaItem, "onLoaded: ${mediaItem.localConfiguration?.uri}, mediaLoadData=$mediaLoadData, loadEventInfo=$loadEventInfo")
    }

    override fun onPlay(
        player: Player,
        mediaItem: MediaItem,
    ) {
        log(mediaItem, "onPlay")
    }

    override fun onPause(
        player: Player,
        mediaItem: MediaItem,
    ) {
        log(mediaItem, "onPause")
    }

    override fun onVideoSizeChange(
        player: Player,
        mediaItem: MediaItem,
        videoSize: VideoSize,
    ) {
        log(mediaItem, "onVideoSizeChange: ${videoSize.width}x${videoSize.height}")
    }

    override fun onDroppedVideoFrames(
        player: Player,
        mediaItem: MediaItem,
        droppedFrames: Int,
    ) {
        log(mediaItem, "onDroppedFrames: $droppedFrames frames")
    }

    override fun onError(
        player: Player,
        mediaItem: MediaItem,
        error: PlaybackException,
    ) {
        log(mediaItem, "onError: ${error.message}")
    }

    private companion object {
        private fun log(
            mediaItem: MediaItem,
            message: String,
        ) {
            DebugLogger.debug("LoggerQosTracker", "[${mediaItem.mediaId}] $message")
        }
    }
}
