/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Period
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MyDummyTracker
import ch.srgssr.pillarbox.player.utils.StringUtil
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

/**
 * Assets
 *
 * @property mediaSource The [MediaSource] used by the player to play something.
 * @property trackersData The [MediaItemTrackerData] to set to the [PillarboxData].
 * @property mediaMetadata The [MediaMetadata] to set to the player media item.
 * @property blockedTimeRanges The [BlockedTimeRange] list.
 */
class Asset internal constructor(
    val mediaSource: Result<MediaSource>,
    val trackersData: MediaItemTrackerData,
    val mediaMetadata: MediaMetadata,
    val blockedTimeRanges: List<BlockedTimeRange>,
    val trackers: List<MediaItemTracker>,
) {

    class Builder {
        var mediaSource: MediaSource? = null
        var trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY
        var mediaMetadata: MediaMetadata = MediaMetadata.EMPTY
        var blockedTimeRanges: List<BlockedTimeRange> = emptyList()
        private val trackers: MutableSet<MediaItemTracker> = mutableSetOf()

        fun addTracker(mediaItemTracker: MediaItemTracker): Builder {
            trackers.add(mediaItemTracker)
            return this
        }

        fun addTrackers(mediaItemTrackers: Collection<MediaItemTracker>): Builder {
            trackers.addAll(mediaItemTrackers)
            return this
        }

        fun build(): Asset {
            addTracker(MyDummyTracker())
            return Asset(
                mediaSource = mediaSource?.let {
                    Result.success(it)
                } ?: Result.failure(IllegalArgumentException("Missing MediaSource")),
                mediaMetadata = mediaMetadata,
                trackersData = trackersData,
                trackers = trackers.toList(),
                blockedTimeRanges = blockedTimeRanges,
            )
        }
    }
}

// For QoS sample we want to listen what happens while item is loading in "background" but no more after it is not the current one.
class EventLoggerTracker(private val data: Any = UUID.randomUUID().toString()) : MediaItemTracker {
    private val listener = object : AnalyticsListener {
        private val window = Window()
        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
            // if (session?.periodUid == eventTime.getUidOfPeriod(Timeline.Window()))
            Log.e(TAG, "onPlayerError ${session?.mediaItem?.mediaMetadata?.title}", error)
        }

        override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
            super.onTimelineChanged(eventTime, reason)
            if (eventTime.timeline.isEmpty) return
            Log.d(TAG, "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)} ${session.mediaItem.mediaMetadata.title}")
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            if (eventTime.timeline.isEmpty) return
            Log.d(TAG, "onPlaybackStateChanged ${StringUtil.playerStateString(state)} ${session.mediaItem.mediaMetadata.title}")
        }

        override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
            if (eventTime.timeline.isEmpty) return
            if (mediaLoadData.dataType == C.DATA_TYPE_MANIFEST && manifestUrl == null && session.periodUid == eventTime.getUidOfPeriod(window)) {
                manifestUrl = loadEventInfo.uri
                Log.d(TAG, "onLoadCompleted ${session.mediaItem.mediaMetadata.title} ${loadEventInfo.uri}")
            }
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            Log.d(
                TAG,
                "onIsPlayingChanged $isPlaying session = ${session.mediaItem.mediaMetadata.title} event = ${window.mediaItem.mediaMetadata.title}"
            )
            Log.d(TAG, "  ${window.firstPeriodIndex} to ${window.lastPeriodIndex}")
            Log.d(TAG, "  ${eventTime.mediaPeriodId?.periodUid}")
            if (window.firstPeriodIndex == window.lastPeriodIndex) return
            val period = Period()
            for (i in window.firstPeriodIndex..window.lastPeriodIndex) {
                eventTime.timeline.getPeriod(i, period, true)
                Log.d(
                    TAG,
                    "  Period $i ${period.windowIndex} - ${period.id} - ${period.uid} ${period.positionInWindowMs.milliseconds} ${
                        period.durationMs.milliseconds
                    }"
                )
            }
        }

        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && newPosition.periodUid == oldPosition.periodUid) {
                Log.d(TAG, "onPositionDiscontinuity repeat stop ${oldPosition.positionMs.milliseconds}")
            }
        }

        override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) {
                Log.d(TAG, "onMediaItemTransition repeated! ${eventTime.eventPlaybackPositionMs.milliseconds}")
            }
        }

        override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        }
    }

    private lateinit var session: PlaybackSessionManager.Session
    private var manifestUrl: Uri? = null

    override fun start(player: ExoPlayer, initialData: Any?) {
        Log.i(TAG, "-- start -- $this")
        // player.addAnalyticsListener(listener)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        Log.i(TAG, "-- stop -- with loadedManifest = $manifestUrl")
        // player.removeAnalyticsListener(listener)
    }

    // Player / Analytics listener will send callback events while "item" is in the playlist! Have to check if the event belongs to this session or
    // not.
    override fun created(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "created ${session.mediaItem.mediaMetadata.title}")
        this.session = session
        pillarboxExoPlayer.addAnalyticsListener(listener)
    }

    override fun cleared(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "cleared ${session.mediaItem.mediaMetadata.title}")
        pillarboxExoPlayer.removeAnalyticsListener(listener)
    }

    override fun start(session: PlaybackSessionManager.Session, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "start ${session.mediaItem.mediaMetadata.title}")
        start(player = pillarboxExoPlayer, null)
        val periodIndex = pillarboxExoPlayer.currentTimeline.getWindow(pillarboxExoPlayer.currentMediaItemIndex, Window()).firstPeriodIndex
        pillarboxExoPlayer.currentTimeline.getUidOfPeriod(periodIndex)
    }

    override fun stop(session: PlaybackSessionManager.SessionInfo, pillarboxExoPlayer: PillarboxExoPlayer) {
        Log.i(TAG, "stop ${session.session.mediaItem.mediaMetadata.title}")
        stop(player = pillarboxExoPlayer, MediaItemTracker.StopReason.Stop, session.position)
    }

    companion object {
        private const val TAG = "Coucou"
    }
}
