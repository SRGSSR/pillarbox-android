/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.getTrackersOrNull

/**
 * Current media item tracker
 *
 * @property player The Player to track current media item
 */
class CurrentMediaItemTracker internal constructor(private val player: ExoPlayer) : AnalyticsListener {

    private var currentMediaItem: MediaItem? = null
        set(value) {
            if (value != field) {
                field?.let { stopSession(it) }
                field = value
                field?.let { startSession(it) }
            }
        }
    private val window = Window()

    init {
        player.addAnalyticsListener(this)
        currentMediaItem = player.currentMediaItem
    }

    private fun startSession(mediaItem: MediaItem) {
        mediaItem.getTrackersOrNull()?.let {
            for (tracker in it) {
                tracker.start(player)
            }
        }
    }

    private fun stopSession(mediaItem: MediaItem) {
        mediaItem.getTrackersOrNull()?.let {
            for (tracker in it) {
                tracker.stop(player)
            }
        }
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
        eventTime.timeline.getWindow(eventTime.windowIndex, window)
        val mediaItem = window.mediaItem
        currentMediaItem = mediaItem
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        currentMediaItem = mediaItem
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        when (state) {
            Player.STATE_IDLE, Player.STATE_ENDED -> currentMediaItem = null
            Player.STATE_READY -> {
                currentMediaItem = eventTime.timeline.getWindow(eventTime.windowIndex, window).mediaItem
            }
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        eventTime.timeline.getWindow(eventTime.windowIndex, window)
        val mediaItem = window.mediaItem
        currentMediaItem = mediaItem
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        currentMediaItem = null
        player.removeAnalyticsListener(this)
    }
}
