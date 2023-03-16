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

/**
 * Current media item tracker
 *
 * @property player The Player to track current media item.
 * @property mediaItemTrackerProvider The MediaItemTrackerProvider that provide new instance of [MediaItemTracker].
 */
internal class CurrentMediaItemTracker internal constructor(
    private val player: ExoPlayer,
    private val mediaItemTrackerProvider: MediaItemTrackerProvider
) : AnalyticsListener {

    private var trackers: MediaItemTrackerList? = null

    private var currentMediaItem: MediaItem? = player.currentMediaItem
        set(value) {
            if (!areEquals(value, field)) {
                field?.let { stopSession() }
                field = value
                field?.let { startSession(it) }
            } else {
                trackers?.let {
                    for (tracker in it.list) {
                        field?.getTrackData()?.getData(tracker)?.let { data ->
                            tracker.update(data)
                        }
                    }
                }
            }
        }

    private val window = Window()

    init {
        player.addAnalyticsListener(this)
        currentMediaItem = player.currentMediaItem
    }

    private fun MediaItem.getTrackData(): MediaItemTrackerData? {
        return localConfiguration?.tag as MediaItemTrackerData?
    }

    /**
     * Are equals only checks mediaId and localConfiguration.uri
     *
     * @param m1
     * @param m2
     * @return
     */
    private fun areEquals(m1: MediaItem?, m2: MediaItem?): Boolean {
        if (m1 == null && m2 == null) return true
        return m1?.mediaId == m2?.mediaId && m1?.localConfiguration?.uri == m2?.localConfiguration?.uri
    }

    private fun startSession(mediaItem: MediaItem) {
        mediaItem.getTrackData()?.let {
            val trackers = MediaItemTrackerList()
            // Create each tracker for this new MediaItem
            for (trackerType in it.trackers) {
                val tracker = mediaItemTrackerProvider.getMediaItemTrackerFactory(trackerType).create()
                trackers.append(tracker)
                tracker.start(player)
                it.getData(tracker)?.let { data ->
                    tracker.update(data)
                }
            }
            this.trackers = trackers
        }
    }

    private fun stopSession() {
        trackers?.let {
            for (tracker in it.list) {
                tracker.stop(player)
            }
        }
        trackers = null
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
