/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.asLongState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.DefaultUpdateInterval
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.availableCommandsAsFlow
import ch.srgssr.pillarbox.player.currentBufferedPercentageAsFlow
import ch.srgssr.pillarbox.player.currentMediaMetadataAsFlow
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.durationAsFlow
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.getCreditAtPosition
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.getAspectRatioAsFlow
import ch.srgssr.pillarbox.player.getCurrentChapterAsFlow
import ch.srgssr.pillarbox.player.getCurrentCreditAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItemIndexAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItemsAsFlow
import ch.srgssr.pillarbox.player.getDeviceInfoAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.getVolumeAsFlow
import ch.srgssr.pillarbox.player.isCurrentMediaItemLiveAsFlow
import ch.srgssr.pillarbox.player.isDeviceMutedAsFlow
import ch.srgssr.pillarbox.player.isPlayingAsFlow
import ch.srgssr.pillarbox.player.mediaItemCountAsFlow
import ch.srgssr.pillarbox.player.playWhenReadyAsFlow
import ch.srgssr.pillarbox.player.playbackStateAsFlow
import ch.srgssr.pillarbox.player.playerErrorAsFlow
import ch.srgssr.pillarbox.player.videoSizeAsFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

/**
 * Observe the [Player.isPlaying] property as a [State].
 *
 * @return A [State] that represents whether the [Player] is currently playing.
 */
@Composable
fun Player.isPlayingAsState(): State<Boolean> {
    val flow = remember(this) {
        isPlayingAsFlow()
    }
    return flow.collectAsState(initial = isPlaying)
}

/**
 * Observe the [Player.getPlayWhenReady] property as a [State].
 *
 * @return A [State] that represents the current 'play when ready' value of the [Player].
 */
@Composable
fun Player.playWhenReadyAsState(): State<Boolean> {
    val flow = remember(this) {
        playWhenReadyAsFlow()
    }
    return flow.collectAsState(initial = playWhenReady)
}

/**
 * Observe the [Player.getPlaybackState] property as a [State].
 *
 * @return A [State] that presents the current playback state of the [Player].
 */
@Composable
fun Player.playbackStateAsState(): IntState {
    val flow = remember(this) {
        playbackStateAsFlow()
    }
    return flow.collectAsState(initial = playbackState).asIntState()
}

/**
 * Observe the [Player.getCurrentPosition] property as a [State].
 *
 * @return A [State] that represents the current position of the [Player].
 */
@Composable
fun Player.currentPositionAsState(): LongState {
    val flow = remember(this) {
        currentPositionAsFlow()
    }
    return flow.collectAsState(initial = currentPosition).asLongState()
}

/**
 * Observe the [Player.getBufferedPercentage] property, adjusted between 0 and 1, as a [State].
 *
 * @param updateInterval The interval at which this value is updated.
 *
 * @return A [State] that represents the buffer percentage (between 0 and 1) of the [Player].
 */
@Composable
fun Player.currentBufferedPercentageAsState(updateInterval: Duration = DefaultUpdateInterval): FloatState {
    val flow = remember(this, updateInterval) {
        currentBufferedPercentageAsFlow(updateInterval)
    }
    return flow.collectAsState(initial = bufferedPercentage / 100f).asFloatState()
}

/**
 * Observe the [Player.getDuration] property as a [State].
 *
 * @return A [State] that represents the duration of the current [MediaItem].
 */
@Composable
fun Player.durationAsState(): LongState {
    val flow = remember(this) {
        durationAsFlow()
    }
    return flow.collectAsState(initial = duration).asLongState()
}

/**
 * Observe the [Player.getAvailableCommands] property as a [State].
 *
 * @return A [State] that represents the currently available commands of the [Player].
 */
@Composable
fun Player.availableCommandsAsState(): State<Commands> {
    val flow = remember(this) {
        availableCommandsAsFlow()
    }
    return flow.collectAsState(initial = availableCommands)
}

/**
 * Observe the [Player.getPlayerError] property as a [State].
 *
 * @return A [State] that represents the current error of the [Player], or `null` if none.
 */
@Composable
fun Player.playerErrorAsState(): State<PlaybackException?> {
    val flow = remember(this) {
        playerErrorAsFlow()
    }
    return flow.collectAsState(initial = playerError)
}

/**
 * Observe the [Player.getMediaItemCount] property as a [State].
 *
 * @return A [State] that represents the current [MediaItem] count of the [Player].
 */
@Composable
fun Player.mediaItemCountAsState(): IntState {
    val flow = remember(this) {
        mediaItemCountAsFlow()
    }
    return flow.collectAsState(initial = mediaItemCount).asIntState()
}

/**
 * Observe whether the [Player] has any media items.
 *
 * @return A [State] that represents whether the [Player] currently has any media items.
 */
@Composable
fun Player.hasMediaItemsAsState(): State<Boolean> {
    val mediaItemCount by mediaItemCountAsState()

    return remember {
        derivedStateOf { mediaItemCount > 0 }
    }
}

/**
 * Observe the [Player.getPlaybackSpeed] property as a [State].
 *
 * @return A [State] that represents the current playback speed of the [Player].
 */
@Composable
fun Player.playbackSpeedAsState(): FloatState {
    val flow = remember(this) {
        getPlaybackSpeedAsFlow()
    }
    return flow.collectAsState(initial = getPlaybackSpeed()).asFloatState()
}

/**
 * Observe the [Player.getMediaMetadata] property as a [State].
 *
 * @return A [State] that represents the metadata of the current [MediaItem] of the [Player].
 */
@Composable
fun Player.currentMediaMetadataAsState(): State<MediaMetadata> {
    val flow = remember(this) {
        currentMediaMetadataAsFlow()
    }
    return flow.collectAsState(initial = mediaMetadata)
}

/**
 * Observe the [Player.getCurrentMediaItemIndex] property as a [State].
 *
 * @return A [State] that represents the current [MediaItem] index.
 */
@Composable
fun Player.currentMediaItemIndexAsState(): IntState {
    val flow = remember(this) {
        getCurrentMediaItemIndexAsFlow()
    }
    return flow.collectAsState(initial = currentMediaItemIndex).asIntState()
}

/**
 * Observe the [Player.getCurrentMediaItems] property as a [State].
 *
 * @return A [State] that represents the current [MediaItem]s of the [Player].
 */
@Composable
fun Player.getCurrentMediaItemsAsState(): State<List<MediaItem>> {
    val flow = remember(this) {
        getCurrentMediaItemsAsFlow()
    }
    return flow.collectAsState(initial = getCurrentMediaItems())
}

/**
 * Observe the [Player.getCurrentMediaItemIndex] property as a [State].
 *
 * @return A [State] that represents the current media item index of the [Player].
 */
@Composable
fun Player.getCurrentMediaItemIndexAsState(): IntState {
    val flow = remember(this) {
        getCurrentMediaItemIndexAsFlow()
    }
    return flow.collectAsState(initial = currentMediaItemIndex).asIntState()
}

/**
 * Observe the [Player.getVideoSize] property as a [State].
 *
 * @return A [State] that represents the video size of the current [MediaItem].
 */
@Composable
fun Player.videoSizeAsState(): State<VideoSize> {
    val flow = remember(this) {
        videoSizeAsFlow()
    }
    return flow.collectAsState(initial = videoSize)
}

/**
 * Observe the aspect ratio of the current [MediaItem] as a [State].
 *
 * @param defaultAspectRatio The aspect ratio when the video size is unknown, or for audio content.
 *
 * @return A [State] that represents the current aspect ratio of the [Player].
 */
@Composable
fun Player.getAspectRatioAsState(defaultAspectRatio: Float): FloatState {
    val flow = remember(this, defaultAspectRatio) {
        getAspectRatioAsFlow(defaultAspectRatio = defaultAspectRatio)
    }
    return flow.collectAsState(initial = defaultAspectRatio).asFloatState()
}

/**
 * Observe the [Player.isCurrentMediaItemLive] property as a [State].
 *
 * @return A [State] that represents whether the current [MediaItem] is a live stream.
 */
@Composable
fun Player.isCurrentMediaItemLiveAsState(): State<Boolean> {
    val flow = remember(this) {
        isCurrentMediaItemLiveAsFlow()
    }
    return flow.collectAsState(initial = isCurrentMediaItemLive)
}

/**
 * Observe the [Player.getChapterAtPosition] property as a [State].
 *
 * @return A [State] that represents the current [Chapter], or `null` if none.
 */
@Composable
fun Player.getCurrentChapterAsState(): State<Chapter?> {
    val flow = remember(this) {
        getCurrentChapterAsFlow()
    }
    return flow.collectAsState(initial = getChapterAtPosition())
}

/**
 * Observe the [Player.getCreditAtPosition] property as a [State].
 *
 * @return A [State] that represents the current [Credit], or `null` if none.
 */
@Composable
fun Player.getCurrentCreditAsState(): State<Credit?> {
    val flow = remember(this) {
        getCurrentCreditAsFlow()
    }
    return flow.collectAsState(initial = getCreditAtPosition())
}

/**
 * Observe the [PillarboxPlayer.getCurrentMetrics] property as a [State].
 *
 * @param updateInterval The interval at which this value is updated.
 *
 * @return A [State] that represents the current metrics of the [PillarboxPlayer], or `null` if none.
 */
@Composable
fun PillarboxPlayer.getPeriodicallyCurrentMetricsAsState(updateInterval: Duration = DefaultUpdateInterval): State<PlaybackMetrics?> {
    return remember(this) {
        currentPositionAsFlow(updateInterval).map { getCurrentMetrics() }
    }.collectAsState(initial = getCurrentMetrics())
}

/**
 * Observe the [Player.getVolume] property as a [State].
 *
 * @return A [State] that represents the current volume.
 */
@Composable
fun Player.getVolumeAsState(): FloatState {
    val flow = remember(this) {
        getVolumeAsFlow()
    }
    return flow.collectAsState(initial = volume).asFloatState()
}

/**
 * Observe the [Player.isDeviceMuted] property as a [State].
 *
 * @return A [State] that represents the current muted state of the device.
 */
@Composable
fun Player.isDeviceMutedAsState(): State<Boolean> {
    val flow = remember(this) {
        isDeviceMutedAsFlow()
    }
    return flow.collectAsState(initial = isDeviceMuted)
}

/**
 * Observe the [Player.getDeviceInfo] property as a [State].
 */
@Composable
fun Player.getDeviceInfoAsState(): State<DeviceInfo> {
    return remember(this) { getDeviceInfoAsFlow() }.collectAsState(deviceInfo)
}
