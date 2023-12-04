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
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.player.availableCommandsAsFlow
import ch.srgssr.pillarbox.player.currentMediaMetadataAsFlow
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.durationAsFlow
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.getAspectRatioAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItemIndexAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItemsAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.isPlayingAsFlow
import ch.srgssr.pillarbox.player.mediaItemCountAsFlow
import ch.srgssr.pillarbox.player.playbackStateAsFlow
import ch.srgssr.pillarbox.player.playerErrorAsFlow
import ch.srgssr.pillarbox.player.shuffleModeEnabledAsFlow
import ch.srgssr.pillarbox.player.videoSizeAsFlow

/**
 * Composable helper function to facilitate compose integration
 */

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun Player.isPlayingAsState(): State<Boolean> {
    val flow = remember(this) {
        isPlayingAsFlow()
    }
    return flow.collectAsState(initial = isPlaying)
}

/**
 * Playback state [Player.getPlaybackState]
 */
@Composable
fun Player.playbackStateAsState(): IntState {
    val flow = remember(this) {
        playbackStateAsFlow()
    }
    return flow.collectAsState(initial = playbackState).asIntState()
}

/**
 * Current position [Player.getCurrentPosition]
 */
@Composable
fun Player.currentPositionAsState(): LongState {
    val flow = remember(this) {
        currentPositionAsFlow()
    }
    return flow.collectAsState(initial = currentPosition).asLongState()
}

/**
 * Duration [Player.getDuration]
 */
@Composable
fun Player.durationAsState(): LongState {
    val flow = remember(this) {
        durationAsFlow()
    }
    return flow.collectAsState(initial = duration).asLongState()
}

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun Player.availableCommandsAsState(): State<Commands> {
    val flow = remember(this) {
        availableCommandsAsFlow()
    }
    return flow.collectAsState(initial = availableCommands)
}

/**
 * Player error [Player.getPlayerError]
 */
@Composable
fun Player.playerErrorAsState(): State<PlaybackException?> {
    val flow = remember(this) {
        playerErrorAsFlow()
    }
    return flow.collectAsState(initial = playerError)
}

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun Player.shuffleModeEnabledAsState(): State<Boolean> {
    val flow = remember(this) {
        shuffleModeEnabledAsFlow()
    }
    return flow.collectAsState(initial = shuffleModeEnabled)
}

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun Player.mediaItemCountAsState(): IntState {
    val flow = remember(this) {
        mediaItemCountAsFlow()
    }
    return flow.collectAsState(initial = mediaItemCount).asIntState()
}

/**
 * @return true if [Player.getMediaItemCount] > 0
 */
@Composable
fun Player.hasMediaItemsAsState(): State<Boolean> {
    val mediaItemCount by mediaItemCountAsState()

    return remember {
        derivedStateOf { mediaItemCount > 0 }
    }
}

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun Player.playbackSpeedAsState(): FloatState {
    val flow = remember(this) {
        getPlaybackSpeedAsFlow()
    }
    return flow.collectAsState(initial = getPlaybackSpeed()).asFloatState()
}

/**
 * Current media metadata [Player.getMediaMetadata]
 */
@Composable
fun Player.currentMediaMetadataAsState(): State<MediaMetadata> {
    val flow = remember(this) {
        currentMediaMetadataAsFlow()
    }
    return flow.collectAsState(initial = mediaMetadata)
}

/**
 * Current media item index as state [Player.getCurrentMediaItem]
 */
@Composable
fun Player.currentMediaItemIndexAsState(): IntState {
    val flow = remember(this) {
        getCurrentMediaItemIndexAsFlow()
    }
    return flow.collectAsState(initial = currentMediaItemIndex).asIntState()
}

/**
 * Get current media items as state [Player.getCurrentMediaItems]
 */
@Composable
fun Player.getCurrentMediaItemsAsState(): State<List<MediaItem>> {
    val flow = remember(this) {
        getCurrentMediaItemsAsFlow()
    }
    return flow.collectAsState(initial = getCurrentMediaItems())
}

/**
 * Video size as state [Player.getVideoSize]
 */
@Composable
fun Player.videoSizeAsState(): State<VideoSize> {
    val flow = remember(this) {
        videoSizeAsFlow()
    }
    return flow.collectAsState(initial = videoSize)
}

/**
 * Get aspect ratio as state computed from [Player.getVideoSize]
 *
 * @param defaultAspectRatio The aspect ratio when video size is unknown or for audio content.
 */
@Composable
fun Player.getAspectRatioAsState(defaultAspectRatio: Float): FloatState {
    val flow = remember(this, defaultAspectRatio) {
        getAspectRatioAsFlow(defaultAspectRatio = defaultAspectRatio)
    }
    return flow.collectAsState(initial = defaultAspectRatio).asFloatState()
}
