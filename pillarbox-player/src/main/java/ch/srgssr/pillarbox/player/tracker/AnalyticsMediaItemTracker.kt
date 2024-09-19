/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.PositionInfo
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerDataOrNull
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker.StopReason
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tracks [Player.getCurrentTracks] to handle [MediaItemTrackerData] changes.
 * @param player The [Player] whose current [Tracks] is tracked for analytics.
 * @param mediaItemTrackerProvider The [MediaItemTrackerProvider] that provide new instance of [MediaItemTracker].
 */
internal class AnalyticsMediaItemTracker(
    private val player: ExoPlayer,
    private val mediaItemTrackerProvider: MediaItemTrackerProvider,
) : Player.Listener {
    private val listener = CurrentMediaItemListener()

    /**
     * Trackers are empty if the tracking session is stopped.
     */
    private var trackers = MediaItemTrackerList()
    private var currentMediaItemTrackerData: MediaItemTrackerData? = null
        set(value) {
            if (field !== value) {
                DebugLogger.info(TAG, "currentMediaItemTrackerData $field -> $value")
                stopSession(StopReason.Stop)
                player.removeAnalyticsListener(listener)
                field = value
                field?.let {
                    if (it.isNotEmpty) {
                        player.addAnalyticsListener(listener)
                        startNewSession(it)
                    }
                }
            }
        }

    var enabled: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (field) {
                currentMediaItemTrackerData = player.currentTracks.getMediaItemTrackerDataOrNull()?.let {
                    startNewSession(data = it)
                    it
                }
            } else {
                stopSession(StopReason.Stop)
            }
        }

    init {
        player.addListener(this)
        currentMediaItemTrackerData = player.currentTracks.getMediaItemTrackerDataOrNull()
    }

    override fun onTracksChanged(tracks: Tracks) {
        currentMediaItemTrackerData = tracks.getMediaItemTrackerDataOrNull()
    }

    fun release() {
        currentMediaItemTrackerData = null
    }

    private fun stopSession(
        stopReason: StopReason,
        positionMs: Long = player.currentPosition,
    ) {
        if (trackers.isEmpty()) return
        DebugLogger.info(TAG, "Stop session $stopReason @${positionMs.milliseconds}")
        for (tracker in trackers) {
            tracker.stop(player, stopReason, positionMs)
        }
        trackers.clear()
    }

    private fun startNewSession(data: MediaItemTrackerData) {
        if (!enabled || data.trackers.isEmpty()) {
            return
        }
        require(trackers.isEmpty())

        DebugLogger.info(TAG, "Start new session for ${player.currentMediaItem?.prettyString()}")
        val trackers = data.trackers
            .map { trackerType ->
                mediaItemTrackerProvider.getMediaItemTrackerFactory(trackerType).create()
                    .also { it.start(player, data.getData(it)) }
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
                    if (trackers.isEmpty() && currentMediaItemTrackerData != null) {
                        startNewSession(data = currentMediaItemTrackerData!!)
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
            oldPosition: PositionInfo,
            newPosition: PositionInfo,
            @Player.DiscontinuityReason reason: Int,
        ) {
            DebugLogger.debug(
                TAG,
                "onPositionDiscontinuity ${StringUtil.discontinuityReasonString(reason)} ${oldPosition.mediaItem?.prettyString()}"
            )

            val oldPositionMs = oldPosition.positionMs
            when (reason) {
                Player.DISCONTINUITY_REASON_REMOVE -> stopSession(StopReason.Stop, oldPositionMs)
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> {
                    stopSession(StopReason.EoF, oldPositionMs)
                    if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) {
                        currentMediaItemTrackerData?.let { startNewSession(it) }
                    }
                }

                else -> {
                    if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                        stopSession(StopReason.Stop, oldPositionMs)
                    }
                }
            }
        }
    }

    private companion object {
        private const val TAG = "AnalyticsMediaItemTracker"
        private fun MediaItem.prettyString(): String {
            return "$mediaId / ${localConfiguration?.uri}"
        }
    }
}
