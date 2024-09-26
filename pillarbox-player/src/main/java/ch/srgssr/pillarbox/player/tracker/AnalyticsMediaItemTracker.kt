/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerDataOrNull
import ch.srgssr.pillarbox.player.utils.DebugLogger

/**
 * Tracks [Player.getCurrentTracks] to handle [MediaItemTrackerData] changes.
 * When player is stopped player state = IDLE the MediaPeriod is destroyed and then prepared is called, it will create a session by calling start.
 *
 * @param player The [Player] whose current [Tracks] is tracked for analytics.
 */
internal class AnalyticsMediaItemTracker(
    private val player: PillarboxExoPlayer,
) : AnalyticsListener {

    /**
     * Trackers are empty if the tracking session is stopped.
     */
    private val trackers = mutableListOf<DelegateMediaItemTracker<*>>()
    private var currentMediaItemTrackerData: MediaItemTrackerData? = null
        set(value) {
            if (field !== value) {
                DebugLogger.info(TAG, "currentMediaItemTrackerData $field -> $value")
                stopSession()
                field = value
                field?.let {
                    if (it.isNotEmpty()) {
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
            currentMediaItemTrackerData = if (field) {
                player.getMediaItemTrackerDataOrNull()
            } else {
                null
            }
        }

    init {
        player.addAnalyticsListener(this)
        currentMediaItemTrackerData = player.getMediaItemTrackerDataOrNull()
    }

    override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        currentMediaItemTrackerData = tracks.getMediaItemTrackerDataOrNull()
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_IDLE) {
            release()
        }
    }

    fun release() {
        currentMediaItemTrackerData = null
    }

    private fun stopSession() {
        if (trackers.isEmpty()) return
        DebugLogger.info(TAG, "Stop session")
        for (tracker in trackers) {
            tracker.stop(player)
        }
        trackers.clear()
    }

    private fun startNewSession(data: MediaItemTrackerData) {
        if (!enabled || data.isEmpty()) {
            return
        }
        require(trackers.isEmpty())
        DebugLogger.info(TAG, "Start new session for ${player.currentMediaItem?.prettyString()}")
        val delegates = data.map {
            DelegateMediaItemTracker(it.value).apply {
                this.start(player, Unit)
            }
        }
        trackers.addAll(delegates)
    }

    private companion object {
        private const val TAG = "AnalyticsMediaItemTracker"
        private fun MediaItem.prettyString(): String {
            return "$mediaId / ${localConfiguration?.uri}"
        }
    }
}

internal class DelegateMediaItemTracker<T>(private val factoryData: FactoryData<T>) : MediaItemTracker<Unit> {
    val tracker: MediaItemTracker<T> = factoryData.factory.create()

    override fun start(player: ExoPlayer, data: Unit) {
        tracker.start(player, factoryData.data)
    }

    override fun stop(player: ExoPlayer) {
        tracker.stop(player)
    }
}
