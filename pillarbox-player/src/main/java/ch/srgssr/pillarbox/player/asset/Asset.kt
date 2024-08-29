/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.util.Log
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger

/**
 * Assets
 *
 * @property mediaSource The [MediaSource] used by the player to play something.
 * @property trackersData The [MediaItemTrackerData] to set to the [PillarboxData].
 * @property mediaMetadata The [MediaMetadata] to set to the player media item.
 * @property blockedTimeRanges The [BlockedTimeRange] list.
 */
class Asset(
    var mediaSource: MediaSource? = null,
    var trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY,
    var mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    var blockedTimeRanges: List<BlockedTimeRange> = emptyList(),
    var error: Throwable? = null,
) {
    val trackers: MutableList<MediaItemTracker> = mutableListOf()

    fun addTracker(mediaItemTracker: MediaItemTracker) {
        trackers.add(mediaItemTracker)
    }

    fun addTrackers(mediaItemTrackers: Collection<MediaItemTracker>) {
        trackers.addAll(mediaItemTrackers)
    }
}

class EventLoggerTracker : MediaItemTracker {
    private val eventLogger = PillarboxEventLogger()
    private val listener = object : AnalyticsListener {
        override fun onPlayerErrorChanged(eventTime: AnalyticsListener.EventTime, error: PlaybackException?) {
            Log.e(TAG, "onPlayerError", error)
        }
    }

    override fun start(player: ExoPlayer, initialData: Any?) {
        Log.i(TAG, "  start")
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        Log.i(TAG, "  stop")
        player.removeAnalyticsListener(eventLogger)
    }

    override fun created(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "created $session")
        pillarboxExoPlayer.addAnalyticsListener(listener)
    }

    override fun cleared(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "cleared $session")
        pillarboxExoPlayer.removeAnalyticsListener(listener)
    }

    override fun start(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "start $session")
        start(player = pillarboxExoPlayer, null)
    }

    override fun stop(session: PlaybackSessionManager.SessionInfo, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "stop $session")
        stop(player = pillarboxExoPlayer, MediaItemTracker.StopReason.Stop, session.position)
    }

    companion object {
        private const val TAG = "Coucou"
    }
}
