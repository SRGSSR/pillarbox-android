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
 * Collects the [playback state][Player.getPlaybackState] as a [Flow].
 *
 * @return A [Flow] emitting the playback state.
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
 * Collects the [playback error][Player.getPlayerError] as a [Flow].
 *
 * @return A [Flow] emitting the playback error.
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
 * Collects whether the player [is playing][Player.isPlaying] as a [Flow].
 *
 * @return A [Flow] emitting whether the player is playing.
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
 * Collects the [duration][Player.getDuration] as a [Flow].
 *
 * @return A [Flow] emitting the duration.
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
 * Collects the [playback speed][Player.getPlaybackSpeed] as a [Flow].
 *
 * @return A [Flow] emitting the playback speed.
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
 * Collects the [available commands][Player.getAvailableCommands] as a [Flow].
 *
 * @return A [Flow] emitting the available commands.
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
 * Collects whether the [shuffle mode is enabled][Player.getShuffleModeEnabled] as a [Flow].
 *
 * @return A [Flow] emitting whether the shuffle mode is enabled.
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
 * Collects the [repeat mode][Player.getRepeatMode] as a [Flow].
 *
 * @return A [Flow] emitting the repeat mode.
 */
fun Player.repeatModeAsFlow(): Flow<@Player.RepeatMode Int> = callbackFlow {
    val listener = object : Listener {
        override fun onRepeatModeChanged(repeatMode: @Player.RepeatMode Int) {
            trySend(repeatMode)
        }
    }
    trySend(repeatMode)
    addPlayerListener(player = this@repeatModeAsFlow, listener)
}

/**
 * Collects the [media item count][Player.getMediaItemCount] as a [Flow].
 *
 * @return A [Flow] emitting the media item count.
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
 * Emits an event every [interval] while the [Player] is playing.
 *
 * @param interval The time interval between emissions.
 * @return A [Flow] that emits at the specified interval while the player is playing.
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
 * Collects the [current position][Player.getCurrentPosition] of the player as a [Flow].
 *
 * @param updateInterval The time interval between emissions, if the player is playing.
 * @return A [Flow] emitting the current position of the player, in milliseconds.
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
 * Collects the [buffered percentage][Player.getBufferedPercentage] as a [Flow].
 *
 * @param updateInterval The time interval between emissions, if the player is playing.
 * @return A [Flow] emitting the buffered percentage.
 */
@Suppress("MagicNumber")
fun Player.currentBufferedPercentageAsFlow(
    updateInterval: Duration = DefaultUpdateInterval
): Flow<Float> = tickerWhilePlayingAsFlow(updateInterval).map {
    bufferedPercentage / 100f
}

/**
 * Collects the [current media item][Player.getCurrentMediaItem] as a [Flow].
 *
 * @return A [Flow] emitting the current media item.
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
 * Collects the [media metadata][Player.getMediaMetadata] as a [Flow].
 *
 * @param withPlaylistMediaMetadata Whether to listen to [Player.Listener.onPlaylistMetadataChanged] too.
 * @return A [Flow] emitting the media metadata.
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
 * Collects the [current media item index][Player.getCurrentMediaItemIndex] as a [Flow].
 *
 * @return A [Flow] emitting the current media item index.
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
 * Collects the [current media items][Player.getCurrentMediaItems] as a [Flow].
 *
 * @return A [Flow] emitting the current media items.
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
 * Collects the [video size][Player.getVideoSize] as a [Flow].
 *
 * @return A [Flow] emitting the video size.
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
 * Collects the aspect ratio of the current video as a [Flow].
 *
 * @param defaultAspectRatio The default aspect ration when the video size is unknown, or the content is not a video.
 * @return A [Flow] emitting the aspect ratio.
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
 * Collects the [track selection parameters][Player.getTrackSelectionParameters] as a [Flow].
 *
 * @return A [Flow] emitting the track selection parameters.
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
 * Collects the [current tracks][Player.getCurrentTracks] as a [Flow].
 *
 * @return A [Flow] emitting the current tracks.
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
 * Collects the [play when ready state][Player.getPlayWhenReady] as a [Flow].
 *
 * @return A [Flow] emitting the play when ready state.
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
 * Collects whether the current media item [is a live stream][Player.isCurrentMediaItemLive] as a [Flow].
 *
 * @return A [Flow] emitting whether the current media item is a live stream.
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
 * Collects the timeline's default position, in milliseconds, as a [Flow].
 *
 * @return A [Flow] emitting the timeline's default position, in milliseconds.
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
 * Collects the [current chapter][Player.getChapterAtPosition] as a [Flow].
 *
 * @return A [Flow] emitting the current chapter.
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
 * Collects the [current credit][Player.getCreditAtPosition] as a [Flow].
 *
 * @return A [Flow] emitting the current credit.
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
 * Collects the [current playback metrics][PillarboxExoPlayer.getCurrentMetrics] as a [Flow].
 *
 * @return A [Flow] emitting the current metrics.
 */
fun PillarboxExoPlayer.currentMetricsAsFlow(): Flow<PlaybackMetrics?> = callbackFlow {
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
 * The default interval between [Flow] emissions.
 */
val DefaultUpdateInterval = 1.seconds
