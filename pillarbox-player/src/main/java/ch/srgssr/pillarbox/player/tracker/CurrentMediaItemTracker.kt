/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.getMediaItemTrackerDataOrNull
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil

/**
 * Current media item tracker
 *
 * Track current media item transition or lifecycle.
 * Tracking session start when current item changed and it is loaded.
 * Tracking session stop when current item changed or when it reached the end of lifecycle.
 *
 * MediaItem asynchronously call this callback after loaded
 *  - onTimelineChanged with reason = [Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE]
 *
 * A MediaItem is considered loaded when it has [MediaItem.LocalConfiguration] not null and it has a tag as [MediaItemTrackerData]
 *
 * @property player The Player for which the current media item must be tracked.
 * @property mediaItemTrackerProvider The MediaItemTrackerProvider that provide new instance of [MediaItemTracker].
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
            if (field) {
                currentMediaItem = player.currentMediaItem
                if (currentMediaItem.canHaveTrackingSession()) {
                    currentMediaItem?.let { startNewSession(it) }
                }
            } else {
                trackers?.let { stopSession(MediaItemTracker.StopReason.Stop, player.currentPosition) }
            }
        }

    private val window = Window()

    init {
        player.addAnalyticsListener(this)
        player.currentMediaItem?.let { startNewSession(it) }
    }

    private fun stopSession(stopReason: MediaItemTracker.StopReason, positionMs: Long) {
        if (currentMediaItem.canHaveTrackingSession()) {
            trackers?.let {
                for (tracker in it.list) {
                    tracker.stop(player, stopReason, positionMs)
                }
            }
        }
        trackers = null
        this.currentMediaItem = null
    }

    private fun startNewSession(mediaItem: MediaItem?) {
        currentMediaItem?.let { stopSession(MediaItemTracker.StopReason.Stop, player.currentPosition) }
        currentMediaItem = mediaItem
        if (enabled && mediaItem.isLoaded()) {
            currentMediaItem?.let {
                startSessionInternal(it)
            }
        }
    }

    private fun updateOrStartSession(mediaItem: MediaItem?) {
        if (!enabled) {
            return
        }
        require(areEqual(currentMediaItem, mediaItem))
        if (currentMediaItem.isLoaded() != mediaItem.isLoaded()) {
            currentMediaItem = mediaItem
            currentMediaItem?.let { startNewSession(it) }
        } else {
            stopSessionInternal()
        }
    }

    private fun stopSessionInternal() {
        trackers?.let {
            for (tracker in it.list) {
                currentMediaItem?.getMediaItemTrackerDataOrNull()?.getData(tracker)?.let { data ->
                    tracker.update(data)
                }
            }
        }
    }

    private fun startSessionInternal(mediaItem: MediaItem) {
        require(trackers == null)
        mediaItem.getMediaItemTrackerDataOrNull()?.let {
            val trackers = MediaItemTrackerList()
            // Create each tracker for this new MediaItem
            for (trackerType in it.trackers) {
                val tracker = mediaItemTrackerProvider.getMediaItemTrackerFactory(trackerType).create()
                trackers.append(tracker)
                tracker.start(player, it.getData(tracker))
            }
            this.trackers = trackers
        }
    }

    private fun updateCurrentItemFromEventTime(eventTime: EventTime) {
        val localItem = if (eventTime.timeline.isEmpty) {
            null
        } else {
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            val mediaItem = window.mediaItem
            mediaItem
        }
        // Current item changed
        if (!areEqual(localItem, currentMediaItem)) {
            startNewSession(localItem)
        } else {
            updateOrStartSession(localItem)
        }
    }

    override fun onTimelineChanged(eventTime: EventTime, reason: Int) {
        DebugLogger.debug(TAG, "onTimelineChanged current = ${toStringMediaItem(currentMediaItem)} ${StringUtil.timelineChangeReasonString(reason)}")
        if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            updateCurrentItemFromEventTime(eventTime)
        }
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        DebugLogger.debug(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)}")
        when (state) {
            Player.STATE_IDLE -> stopSession(MediaItemTracker.StopReason.Stop, player.currentPosition)
            Player.STATE_ENDED -> stopSession(MediaItemTracker.StopReason.EoF, player.currentPosition)
            Player.STATE_READY -> {
                updateCurrentItemFromEventTime(eventTime)
            }
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        DebugLogger.debug(
            TAG,
            "onPositionDiscontinuity (${oldPosition.mediaItemIndex}, ${oldPosition.positionMs}) " +
                "=> (${newPosition.mediaItemIndex}, ${newPosition.positionMs})"
        )
        val oldPositionMs = oldPosition.positionMs
        when (reason) {
            Player.DISCONTINUITY_REASON_REMOVE -> stopSession(MediaItemTracker.StopReason.Stop, oldPositionMs)
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> stopSession(MediaItemTracker.StopReason.EoF, oldPositionMs)
            Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT, Player.DISCONTINUITY_REASON_INTERNAL, Player
                .DISCONTINUITY_REASON_SKIP -> {
                if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                    stopSession(MediaItemTracker.StopReason.Stop, oldPositionMs)
                }
            }
        }
    }

    /**
     * On media item transition is called just after onPositionDiscontinuity
     */
    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(TAG, "onMediaItemTransition ${toStringMediaItem(mediaItem)} ${StringUtil.mediaItemTransitionReasonString(reason)} ")
        mediaItem?.let { startNewSession(mediaItem) }
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        DebugLogger.debug(TAG, "onPlayerReleased")
        player.removeAnalyticsListener(this)
        stopSession(MediaItemTracker.StopReason.Stop)
    }

    companion object {
        private const val TAG = "CurrentItemTracker"

        /**
         * Are equals only checks mediaId and localConfiguration.uri
         *
         * @param m1
         * @param m2
         * @return
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun areEqual(m1: MediaItem?, m2: MediaItem?): Boolean {
            if (m1 == null && m2 == null) return true
            return m1?.getIdentifier() == m2?.getIdentifier()
        }

        private fun MediaItem?.isLoaded(): Boolean {
            return this?.localConfiguration != null
        }

        private fun MediaItem?.canHaveTrackingSession(): Boolean {
            return this?.getMediaItemTrackerDataOrNull() != null
        }

        private fun MediaItem.getIdentifier(): String? {
            return if (mediaId == MediaItem.DEFAULT_MEDIA_ID) return localConfiguration?.uri?.toString() else mediaId
        }

        private fun toStringMediaItem(mediaItem: MediaItem?): String {
            return "media id = ${mediaItem?.mediaId} loaded = ${mediaItem?.localConfiguration?.uri != null}"
        }
    }
}
