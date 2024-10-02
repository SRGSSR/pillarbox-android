/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("TooManyFunctions")

package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.computeAspectRatioOrNull
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.getCreditAtPosition
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.tracks.videoTracks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Playback state [Player.getPlaybackState] as flow.
 */
fun Player.playbackStateAsFlow(): Flow<Int> = callbackFlow {
    val listener = object : Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            trySend(playbackState)
        }
    }
    trySend(playbackState)
    addPlayerListener(player = this@playbackStateAsFlow, listener)
}

/**
 * PlayerError [Player.getPlayerError] as Flow.
 */
fun Player.playerErrorAsFlow(): Flow<PlaybackException?> = callbackFlow {
    val listener = object : Listener {
        override fun onPlayerErrorChanged(error: PlaybackException?) {
            trySend(error)
        }
    }
    trySend(playerError)
    addPlayerListener(player = this@playerErrorAsFlow, listener)
}

/**
 * Is playing [Player.isPlaying] as Flow.
 */
fun Player.isPlayingAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            trySend(isPlaying)
        }
    }
    trySend(isPlaying)
    addPlayerListener(player = this@isPlayingAsFlow, listener)
}

/**
 * Duration [Player.getDuration] as Flow.
 */
fun Player.durationAsFlow(): Flow<Long> = callbackFlow {
    val listener = object : Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            trySend(duration)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                trySend(duration)
            }
        }
    }
    trySend(duration)
    addPlayerListener(player = this@durationAsFlow, listener)
}

/**
 * Playback speed [Player.getPlaybackSpeed] as Flow.
 */
fun Player.getPlaybackSpeedAsFlow(): Flow<Float> = callbackFlow {
    val listener = object : Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            trySend(playbackParameters.speed)
        }
    }
    trySend(getPlaybackSpeed())
    addPlayerListener(player = this@getPlaybackSpeedAsFlow, listener)
}

/**
 * Available commands [Player.getAvailableCommands] as Flow.
 */
fun Player.availableCommandsAsFlow(): Flow<Player.Commands> = callbackFlow {
    val listener = object : Listener {
        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            trySend(availableCommands)
        }
    }
    trySend(availableCommands)
    addPlayerListener(player = this@availableCommandsAsFlow, listener)
}

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled] as Flow.
 */
fun Player.shuffleModeEnabledAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Listener {
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            trySend(shuffleModeEnabled)
        }
    }
    trySend(shuffleModeEnabled)
    addPlayerListener(player = this@shuffleModeEnabledAsFlow, listener)
}

/**
 * Media item count [Player.getMediaItemCount] as Flow.
 */
fun Player.mediaItemCountAsFlow(): Flow<Int> = callbackFlow {
    val listener = object : Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                trySend(mediaItemCount)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            trySend(mediaItemCount)
        }
    }
    trySend(mediaItemCount)
    addPlayerListener(player = this@mediaItemCountAsFlow, listener)
}

/**
 * Ticker emits event every [interval] when [Player.isPlaying] is true.
 * Emit a value once at least once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Player.tickerWhilePlayingAsFlow(
    interval: Duration = DefaultUpdateInterval
): Flow<Unit> = isPlayingAsFlow().transformLatest { isPlaying ->
    while (currentCoroutineContext().isActive && isPlaying) {
        emit(Unit)
        delay(interval)
    }
}

/**
 * Current position of the player updates every [updateInterval] when it is playing.
 * Send current position once if not playing.
 */
fun Player.currentPositionAsFlow(updateInterval: Duration = DefaultUpdateInterval): Flow<Long> =
    merge(
        flow { if (!isPlaying) emit(currentPosition) },
        tickerWhilePlayingAsFlow(updateInterval).map {
            currentPosition
        },
        positionChangedFlow()
    )

private fun Player.positionChangedFlow(): Flow<Long> = callbackFlow {
    val listener = object : Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            trySend(newPosition.positionMs)
        }
    }
    addPlayerListener(player = this@positionChangedFlow, listener)
}.distinctUntilChanged()

/**
 * Current buffered percentage as flow [Player.getBufferedPercentage]
 *
 * @param updateInterval The update interval
 */
@Suppress("MagicNumber")
fun Player.currentBufferedPercentageAsFlow(
    updateInterval: Duration = DefaultUpdateInterval
): Flow<Float> = tickerWhilePlayingAsFlow(updateInterval).map {
    bufferedPercentage / 100f
}

/**
 * Current media metadata as flow [Player.getCurrentMediaItem]
 */
fun Player.currentMediaItemAsFlow(): Flow<MediaItem?> = callbackFlow {
    val listener = object : Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            trySend(mediaItem)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                trySend(currentMediaItem)
            }
        }
    }
    trySend(currentMediaItem)
    addPlayerListener(player = this@currentMediaItemAsFlow, listener)
}

/**
 * Current media metadata as flow [Player.getMediaMetadata]
 *
 * @param withPlaylistMediaMetadata try to listen [Player.Listener.onPlaylistMetadataChanged] too.
 */
fun Player.currentMediaMetadataAsFlow(withPlaylistMediaMetadata: Boolean = false): Flow<MediaMetadata> = callbackFlow {
    val listener = object : Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            trySend(mediaMetadata)
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            if (withPlaylistMediaMetadata) {
                trySend(mediaMetadata)
            }
        }
    }
    trySend(mediaMetadata)
    addPlayerListener(player = this@currentMediaMetadataAsFlow, listener)
}

/**
 * Get current media item index as flow [Player.getCurrentMediaItemIndex]
 */
fun Player.getCurrentMediaItemIndexAsFlow(): Flow<Int> = callbackFlow {
    val listener = object : Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            trySend(currentMediaItemIndex)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                trySend(currentMediaItemIndex)
            }
        }
    }
    trySend(currentMediaItemIndex)
    addPlayerListener(player = this@getCurrentMediaItemIndexAsFlow, listener)
}

/**
 * Get current media items as flow [Player.getCurrentMediaItems]
 */
fun Player.getCurrentMediaItemsAsFlow(): Flow<List<MediaItem>> = callbackFlow {
    val listener = object : Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            trySend(getCurrentMediaItems())
        }
    }
    trySend(getCurrentMediaItems())
    addPlayerListener(player = this@getCurrentMediaItemsAsFlow, listener)
}

/**
 * Get video size as flow [Player.getVideoSize]
 */
fun Player.videoSizeAsFlow(): Flow<VideoSize> = callbackFlow {
    val listener = object : Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            trySend(videoSize)
        }
    }
    trySend(videoSize)
    addPlayerListener(player = this@videoSizeAsFlow, listener)
}

/**
 * Get aspect ratio of the current video as [Flow].
 *
 * @param defaultAspectRatio The aspect ratio when the video size is unknown, or for audio content.
 */
fun Player.getAspectRatioAsFlow(defaultAspectRatio: Float): Flow<Float> {
    return combine(
        getCurrentTracksAsFlow(),
        videoSizeAsFlow(),
    ) { currentTracks, videoSize ->
        currentTracks.getVideoAspectRatioOrNull()
            ?: videoSize.computeAspectRatioOrNull()
            ?: defaultAspectRatio
    }.distinctUntilChanged()
}

/**
 * Get track selection parameters as flow [Player.getTrackSelectionParameters]
 */
fun Player.getTrackSelectionParametersAsFlow(): Flow<TrackSelectionParameters> = callbackFlow {
    val listener = object : Listener {
        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            trySend(parameters)
        }
    }

    trySend(trackSelectionParameters)
    addPlayerListener(player = this@getTrackSelectionParametersAsFlow, listener)
}

/**
 * Get current tracks as flow [Player.getCurrentTracks]
 */
fun Player.getCurrentTracksAsFlow(): Flow<Tracks> = callbackFlow {
    val listener = object : Listener {
        override fun onTracksChanged(tracks: Tracks) {
            trySend(tracks)
        }
    }
    trySend(currentTracks)
    addPlayerListener(player = this@getCurrentTracksAsFlow, listener)
}

/**
 * Play when ready as flow [Player.getPlayWhenReady]
 */
fun Player.playWhenReadyAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            trySend(playWhenReady)
        }
    }
    trySend(playWhenReady)
    addPlayerListener(this@playWhenReadyAsFlow, listener)
}

/**
 * @return `true` if current media item is a live stream.
 */
fun Player.isCurrentMediaItemLiveAsFlow(): Flow<Boolean> = callbackFlow {
    val listener = object : Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            trySend(player.isCurrentMediaItemLive)
        }
    }
    trySend(isCurrentMediaItemLive)
    addPlayerListener(this@isCurrentMediaItemLiveAsFlow, listener)
}.distinctUntilChanged()

/**
 * @return The current default position as flow.
 * @see Timeline.Window.getDefaultPositionMs
 */
fun Player.getCurrentDefaultPositionAsFlow(): Flow<Long> = callbackFlow {
    val window = Timeline.Window()
    val listener = object : Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                )
            ) {
                if (player.currentTimeline.isEmpty) {
                    trySend(C.TIME_UNSET)
                } else {
                    trySend(player.currentTimeline.getWindow(player.currentMediaItemIndex, window).defaultPositionMs)
                }
            }
        }
    }
    if (currentTimeline.isEmpty) {
        trySend(C.TIME_UNSET)
    } else {
        trySend(currentTimeline.getWindow(currentMediaItemIndex, window).defaultPositionMs)
    }
    addPlayerListener(this@getCurrentDefaultPositionAsFlow, listener)
}.distinctUntilChanged()

/**
 * @return Get the current chapter as flow, when the current chapter changes.
 */
fun Player.getCurrentChapterAsFlow(): Flow<Chapter?> = callbackFlow {
    val listener = object : PillarboxPlayer.Listener {
        override fun onChapterChanged(chapter: Chapter?) {
            trySend(chapter)
        }
    }
    trySend(getChapterAtPosition())
    addPlayerListener(this@getCurrentChapterAsFlow, listener)
}

/**
 * @return Get the current credit as flow, when the credit changes.
 */
fun Player.getCurrentCreditAsFlow(): Flow<Credit?> = callbackFlow {
    val listener = object : PillarboxPlayer.Listener {
        override fun onCreditChanged(credit: Credit?) {
            trySend(credit)
        }
    }
    trySend(getCreditAtPosition())
    addPlayerListener(this@getCurrentCreditAsFlow, listener)
}

/**
 * @return Get the current [PlaybackMetrics] as a [Flow].
 */
fun PillarboxExoPlayer.currentMetricsAsFlow(): Flow<PlaybackMetrics?> = callbackFlow<PlaybackMetrics?> {
    val listener = object : Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            trySend(getCurrentMetrics())
        }
    }
    trySend(getCurrentMetrics())
    addPlayerListener(this@currentMetricsAsFlow, listener)
}.distinctUntilChanged()

private suspend fun <T> ProducerScope<T>.addPlayerListener(player: Player, listener: Listener) {
    player.addListener(listener)
    awaitClose {
        player.removeListener(listener)
    }
}

private fun Tracks.getVideoAspectRatioOrNull(): Float? {
    val format = videoTracks.find { it.isSelected }?.format

    return if (format == null || format.height <= 0 || format.width <= 0) {
        null
    } else {
        format.width * format.pixelWidthHeightRatio / format.height.toFloat()
    }
}

/**
 * Default update interval.
 */
val DefaultUpdateInterval = 1.seconds
