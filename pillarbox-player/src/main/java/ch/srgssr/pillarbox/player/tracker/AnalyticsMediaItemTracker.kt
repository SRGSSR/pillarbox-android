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
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerDataOrNull
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker.StopReason
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlin.time.Duration.Companion.milliseconds

/**
 * Custom [CurrentMediaItemTagTracker.Callback] to manage analytics.
 *
 * @param player The [Player] whose current [MediaItem] is tracked for analytics.
 * @param mediaItemTrackerProvider The [MediaItemTrackerProvider] that provide new instance of [MediaItemTracker].
 */
internal class AnalyticsMediaItemTracker(
    private val player: ExoPlayer,
    private val mediaItemTrackerProvider: MediaItemTrackerProvider,
) : CurrentMediaItemTagTracker.Callback {
    private val listener = CurrentMediaItemListener()

    /**
     * Trackers are empty if the tracking session is stopped.
     */
    private var trackers = MediaItemTrackerList()

    /**
     * Current [MediaItem].
     * Detect `mediaId` changes or URLs if no `mediaId`.
     */
    private var currentMediaItem: MediaItem? = null

    private var hasAnalyticsListener = false

    var enabled: Boolean = true
        set(value) {
            if (field == value) {
                return
            }

            field = value
            if (field) {
                player.currentMediaItem?.let { setMediaItem(it) }
            } else {
                stopSession(StopReason.Stop)
            }
        }

    override fun onTagChanged(
        mediaItem: MediaItem?,
        tag: Any?,
    ) {
        if (mediaItem == null) {
            stopSession(StopReason.Stop)
        } else if (tag != null) {
            if (!hasAnalyticsListener) {
                player.addAnalyticsListener(listener)

                hasAnalyticsListener = true
            }

            setMediaItem(mediaItem)
        }
    }

    private fun setMediaItem(mediaItem: MediaItem) {
        if (!areEqual(mediaItem, currentMediaItem)) {
            stopSession(StopReason.Stop)
        }

        if (mediaItem.canHaveTrackingSession() && currentMediaItem?.getMediaItemTrackerDataOrNull() == null) {
            startNewSession(mediaItem)
        }

        // Update the current MediaItem with tracker data
        this.currentMediaItem = mediaItem
    }

    private fun stopSession(
        stopReason: StopReason,
        positionMs: Long = player.currentPosition,
    ) {
        DebugLogger.info(TAG, "Stop trackers $stopReason @${positionMs.milliseconds}")

        for (tracker in trackers) {
            tracker.stop(player, stopReason, positionMs)
        }

        trackers.clear()
        currentMediaItem = null
    }

    private fun startNewSession(mediaItem: MediaItem) {
        if (!enabled) {
            return
        }

        require(trackers.isEmpty())

        DebugLogger.info(TAG, "Start new session for ${mediaItem.prettyString()}")

        // Create each tracker for this new MediaItem
        val mediaItemTrackerData = mediaItem.getMediaItemTrackerDataOrNull() ?: return
        val trackers = mediaItemTrackerData.trackers
            .map { trackerType ->
                mediaItemTrackerProvider.getMediaItemTrackerFactory(trackerType).create()
                    .also { it.start(player, mediaItemTrackerData.getData(it)) }
            }

        this.trackers.addAll(trackers)
    }

    private inner class CurrentMediaItemListener : AnalyticsListener {
        override fun onPlaybackStateChanged(
            eventTime: AnalyticsListener.EventTime,
            @Player.State playbackState: Int,
        ) {
            DebugLogger.debug(
                TAG,
                "onPlaybackStateChanged ${StringUtil.playerStateString(playbackState)} ${player.currentMediaItem?.prettyString()}"
            )

            when (playbackState) {
                Player.STATE_ENDED -> stopSession(StopReason.EoF)
                Player.STATE_IDLE -> stopSession(StopReason.Stop)
                Player.STATE_READY -> {
                    if (currentMediaItem == null) {
                        player.currentMediaItem?.let { setMediaItem(it) }
                    }
                }

                else -> Unit
            }
        }

        /*
         * On position discontinuity handle stop session if required
         */
        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @Player.DiscontinuityReason reason: Int,
        ) {
            DebugLogger.debug(
                TAG,
                "onPositionDiscontinuity ${StringUtil.discontinuityReasonString(reason)} ${player.currentMediaItem?.prettyString()}"
            )

            val oldPositionMs = oldPosition.positionMs
            when (reason) {
                Player.DISCONTINUITY_REASON_REMOVE -> stopSession(StopReason.Stop, oldPositionMs)
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> stopSession(StopReason.EoF, oldPositionMs)
                else -> {
                    if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                        stopSession(StopReason.Stop, oldPositionMs)
                    }
                }
            }
        }

        /*
         * Event received after position_discontinuity
         * if MediaItemTracker are using AnalyticsListener too
         * They may received discontinuity for media item transition.
         */
        override fun onMediaItemTransition(
            eventTime: AnalyticsListener.EventTime,
            mediaItem: MediaItem?,
            @Player.MediaItemTransitionReason reason: Int,
        ) {
            DebugLogger.debug(
                TAG,
                "onMediaItemTransition ${StringUtil.mediaItemTransitionReasonString(reason)} ${player.currentMediaItem?.prettyString()}"
            )

            if (mediaItem == null) {
                stopSession(StopReason.Stop)
            } else {
                setMediaItem(mediaItem)
            }
        }
    }

    internal companion object {
        private const val TAG = "AnalyticsMediaItemTracker"

        private fun MediaItem.prettyString(): String {
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
