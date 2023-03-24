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
    private var currentMediaItem: MediaItem? = player.currentMediaItem
        set(value) {
            when {
                !enabled -> field = value
                !areEqual(field, value) -> {
                    field?.let { if (it.canHaveTrackingSession()) stopSession() }
                    field = value
                    field?.let {
                        if (it.canHaveTrackingSession()) {
                            startSession(it)
                        }
                    }
                }
                field.isLoaded() != value.isLoaded() -> {
                    if (field.canHaveTrackingSession()) {
                        field?.let { stopSession() }
                    }
                    field = value
                    if (field.canHaveTrackingSession()) {
                        field?.let { startSession(it) }
                    }
                }
                else -> {
                    field?.let { updateSession(it) }
                }
            } // When
        }

    var enabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    if (currentMediaItem.canHaveTrackingSession()) {
                        currentMediaItem?.let { startSession(it) }
                    }
                } else {
                    trackers?.let { stopSession() }
                }
            }
        }

    private val window = Window()

    init {
        player.addAnalyticsListener(this)
        currentMediaItem = player.currentMediaItem
    }

    private fun updateSession(mediaItem: MediaItem) {
        trackers?.let {
            for (tracker in it.list) {
                mediaItem.getMediaItemTrackerDataOrNull()?.getData(tracker)?.let { data ->
                    tracker.update(data)
                }
            }
        }
    }

    private fun startSession(mediaItem: MediaItem) {
        require(trackers == null)
        mediaItem.getMediaItemTrackerDataOrNull()?.let {
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
        requireNotNull(trackers)
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
        DebugLogger.debug(TAG, "onTimelineChanged current = ${toStringMediaItem(mediaItem)} ${StringUtil.timelineChangeReasonString(reason)}")
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(TAG, "onMediaItemTransition ${toStringMediaItem(mediaItem)} ${StringUtil.mediaItemTransitionReasonString(reason)} ")
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
            currentMediaItem = null
        }
        currentMediaItem = mediaItem
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        DebugLogger.debug(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)}")
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
        val oldIndex = oldPosition.mediaItemIndex
        val oldId = oldPosition.mediaItem?.getIdentifier()
        val newIndex = newPosition.mediaItemIndex
        val newId = newPosition.mediaItem?.getIdentifier()
        if (oldIndex != newIndex || newId != oldId) {
            currentMediaItem = null
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            val mediaItem = window.mediaItem
            currentMediaItem = mediaItem
        }
        DebugLogger.debug(TAG, "onPositionDiscontinuity ($oldIndex $oldId) -> ($newIndex $newId) ${StringUtil.discontinuityReasonString(reason)}")
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        DebugLogger.debug(TAG, "onPlayerReleased")
        currentMediaItem = null
        player.removeAnalyticsListener(this)
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
