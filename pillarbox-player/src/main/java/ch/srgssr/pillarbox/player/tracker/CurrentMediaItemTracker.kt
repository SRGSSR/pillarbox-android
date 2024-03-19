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
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlin.time.Duration.Companion.milliseconds

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
            if (!field) {
                stopSession(MediaItemTracker.StopReason.Stop)
            } else {
                player.currentMediaItem?.let { setMediaItem(it) }
            }
        }

    init {
        player.addAnalyticsListener(this)
        player.currentMediaItem?.let { setMediaItem(it) }
    }

    private fun setMediaItem(mediaItem: MediaItem) {
        if (!areEqual(mediaItem, currentMediaItem)) {
            stopSession(MediaItemTracker.StopReason.Stop)
            currentMediaItem = mediaItem
            if (mediaItem.canHaveTrackingSession()) {
                startNewSession(mediaItem)
            }
            return
        }
        if (mediaItem.canHaveTrackingSession() && currentMediaItem?.getMediaItemTrackerDataOrNull() == null) {
            startNewSession(mediaItem)
            // Update current media item with tracker data
            this.currentMediaItem = mediaItem
        }
    }

    private fun stopSession(stopReason: MediaItemTracker.StopReason, positionMs: Long = player.currentPosition) {
        trackers?.let {
            DebugLogger.info(TAG, "stop trackers $stopReason @${positionMs.milliseconds}")
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
        DebugLogger.info(TAG, "start new session for ${mediaItem.prettyString()}")

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
        DebugLogger.debug(
            TAG,
            "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)} ${player.currentMediaItem.prettyString()}"
        )
        if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            player.currentMediaItem?.let { setMediaItem(it) }
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, playbackState: Int) {
        DebugLogger.debug(
            TAG,
            "onPlaybackStateChanged ${StringUtil.playerStateString(playbackState)} ${player.currentMediaItem.prettyString()}"
        )
        when (playbackState) {
            Player.STATE_ENDED -> stopSession(MediaItemTracker.StopReason.EoF)
            Player.STATE_IDLE -> stopSession(MediaItemTracker.StopReason.Stop)
            Player.STATE_READY -> {
                if (currentMediaItem == null) {
                    player.currentMediaItem?.let { setMediaItem(it) }
                }
            }

            else -> {
                // Nothing
            }
        }
    }

    /*
     * On position discontinuity handle stop session if required
     */
    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        DebugLogger.debug(TAG, "onPositionDiscontinuity ${StringUtil.discontinuityReasonString(reason)} ${player.currentMediaItem.prettyString()}")
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

    /*
     * Event received after position_discontinuity
     * if MediaItemTracker are using AnalyticsListener too
     * They may received discontinuity for media item transition.
     */
    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        DebugLogger.debug(
            TAG,
            "onMediaItemTransition ${StringUtil.mediaItemTransitionReasonString(reason)} ${player.currentMediaItem.prettyString()}"
        )
        mediaItem?.let {
            setMediaItem(it)
        }
    }

    internal companion object {
        private const val TAG = "CurrentMediaItemTracker"
        private fun MediaItem?.prettyString(): String {
            if (this == null) return "null"
            return "$mediaId / ${localConfiguration?.uri} ${getMediaItemTrackerDataOrNull()}"
        }

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
                else ->
                    m1.mediaId == m2.mediaId &&
                        m1.buildUpon().setTag(null).build().localConfiguration?.uri == m2.buildUpon().setTag(null).build().localConfiguration?.uri
            }
        }

        private fun MediaItem.canHaveTrackingSession(): Boolean {
            return this.getMediaItemTrackerDataOrNull() != null
        }
    }
}
