/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerDataOrNull

/**
 * Current media item tracker
 *
 * Track current media item transition or lifecycle.
 * Tracking session start when current item changed and have [MediaItemTrackerData] set.
 * Tracking session stop when current item changed or when it reached the end of lifecycle.
 *
 * MediaItem asynchronously call this callback after loaded
 *  - onTimelineChanged with reason = [Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE]
 *
 * @param player The Player for which the current media item must be tracked.
 * @param mediaItemTrackerProvider The MediaItemTrackerProvider that provide new instance of [MediaItemTracker].
 */
internal class CurrentMediaItemTracker internal constructor(
    private val player: ExoPlayer,
    private val mediaItemTrackerProvider: MediaItemTrackerProvider
) : AnalyticsListener {

    /**
     * Trackers are null if tracking session is stopped!
     */
    private var trackers: MediaItemTrackerList? = null

    /**
     * Current media item
     * Detect mediaId changes or urls if no mediaId
     */
    private var currentMediaItem: MediaItem? = null

    var enabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            setMediaItem(player.currentMediaItem)
        }

    init {
        player.addAnalyticsListener(this)
        player.currentMediaItem?.let { startNewSession(it) }
    }

    /**
     * Set media item if has not tracking data, set to null
     */
    private fun setMediaItem(mediaItem: MediaItem?) {
        if (enabled && mediaItem != null && mediaItem.canHaveTrackingSession()) {
            if (!areEqual(currentMediaItem, mediaItem)) {
                currentItemChange(currentMediaItem, mediaItem)
                currentMediaItem = mediaItem
            }
        } else {
            currentMediaItem?.let {
                stopSession(MediaItemTracker.StopReason.Stop, player.currentPosition)
            }
        }
    }

    private fun currentItemChange(lastMediaItem: MediaItem?, newMediaItem: MediaItem) {
        if (lastMediaItem == null) {
            startNewSession(newMediaItem)
            return
        }
        if (lastMediaItem.mediaId == newMediaItem.mediaId || lastMediaItem.getMediaItemTrackerData() != newMediaItem.getMediaItemTrackerData()) {
            maybeUpdateData(lastMediaItem, newMediaItem)
        } else {
            stopSession(MediaItemTracker.StopReason.Stop)
            startNewSession(newMediaItem)
        }
    }

    /**
     * Maybe update data
     *
     * Don't start or stop if new tracker data is added. Only update existing trackers with new data.
     */
    private fun maybeUpdateData(lastMediaItem: MediaItem, newMediaItem: MediaItem) {
        trackers?.let {
            val lastTrackerData = lastMediaItem.getMediaItemTrackerData()
            val newTrackerData = newMediaItem.getMediaItemTrackerData()
            for (tracker in it) {
                val newData = newTrackerData.getData(tracker) ?: continue
                val oldData = lastTrackerData.getData(tracker)
                if (newData != oldData) {
                    tracker.update(newData)
                }
            }
        }
    }

    private fun stopSession(stopReason: MediaItemTracker.StopReason, positionMs: Long = player.currentPosition) {
        trackers?.let {
            for (tracker in it) {
                tracker.stop(player, stopReason, positionMs)
            }
        }
        trackers = null
        currentMediaItem = null
    }

    private fun startNewSession(mediaItem: MediaItem) {
        if (!enabled) return
        require(trackers == null)
        mediaItem.getMediaItemTrackerData().also { trackerData ->
            val trackers = MediaItemTrackerList()
            // Create each tracker for this new MediaItem
            for (trackerType in trackerData.trackers) {
                val tracker = mediaItemTrackerProvider.getMediaItemTrackerFactory(trackerType).create()
                trackers.append(tracker)
                tracker.start(player, trackerData.getData(tracker))
            }
            this.trackers = trackers
        }
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
        setMediaItem(player.currentMediaItem)
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, playbackState: Int) {
        when (playbackState) {
            Player.STATE_ENDED -> stopSession(MediaItemTracker.StopReason.EoF)
            Player.STATE_IDLE -> stopSession(MediaItemTracker.StopReason.Stop)
            Player.STATE_READY -> if (currentMediaItem == null) setMediaItem(player.currentMediaItem)
            else -> {
                // Nothing
            }
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        val oldPositionMs = oldPosition.positionMs
        when (reason) {
            Player.DISCONTINUITY_REASON_REMOVE -> stopSession(MediaItemTracker.StopReason.Stop, oldPositionMs)
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> stopSession(MediaItemTracker.StopReason.EoF, oldPositionMs)
            else -> {
                if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                    stopSession(MediaItemTracker.StopReason.Stop, oldPositionMs)
                }
            }
        }
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        setMediaItem(player.currentMediaItem)
    }

    internal companion object {
        /**
         * Are equals only checks mediaId and localConfiguration.uri
         *
         * @param m1
         * @param m2
         * @return
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun areEqual(m1: MediaItem?, m2: MediaItem?): Boolean {
            return when {
                m1 == null && m2 == null -> true
                m1 == null || m2 == null -> false
                else -> m1.getIdentifier() == m2.getIdentifier() && m1.localConfiguration == m2.localConfiguration
            }
        }

        private fun MediaItem.canHaveTrackingSession(): Boolean {
            return this.getMediaItemTrackerDataOrNull() != null
        }

        private fun MediaItem.getIdentifier(): String? {
            return if (mediaId == MediaItem.DEFAULT_MEDIA_ID) localConfiguration?.uri?.toString() else mediaId
        }
    }
}
