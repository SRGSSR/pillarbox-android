/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import ch.srgssr.pillarbox.player.getAspectRatioAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItemIndexAsFlow
import ch.srgssr.pillarbox.player.getCurrentMediaItems
import ch.srgssr.pillarbox.player.getCurrentMediaItemsAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeed
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
fun Player.isPlayingAsState(): Boolean {
    val flow = remember(this) {
        isPlayingAsFlow()
    }
    return flow.collectAsState(initial = isPlaying).value
}

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun Player.playbackStateAsState(): Int {
    val flow = remember(this) {
        playbackStateAsFlow()
    }
    return flow.collectAsState(initial = playbackState).value
}

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun Player.currentPositionAsState(): Long {
    val flow = remember(this) {
        currentPositionAsFlow()
    }
    return flow.collectAsState(initial = currentPosition).value
}

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun Player.durationAsState(): Long {
    val flow = remember(this) {
        durationAsFlow()
    }
    return flow.collectAsState(initial = duration).value
}

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun Player.availableCommandsAsState(): Commands {
    val flow = remember(this) {
        availableCommandsAsFlow()
    }
    return flow.collectAsState(initial = availableCommands).value
}

/**
 * Error [Player.getPlayerError]
 */
@Composable
fun Player.playerErrorAsState(): PlaybackException? {
    val flow = remember(this) {
        playerErrorAsFlow()
    }
    return flow.collectAsState(initial = playerError).value
}

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun Player.shuffleModeEnabledAsState(): Boolean {
    val flow = remember(this) {
        shuffleModeEnabledAsFlow()
    }
    return flow.collectAsState(initial = shuffleModeEnabled).value
}

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun Player.mediaItemCountAsState(): Int {
    val flow = remember(this) {
        mediaItemCountAsFlow()
    }
    return flow.collectAsState(initial = mediaItemCount).value
}

/**
 * @return true if [mediaItemCountAsState] > 0
 */
@Composable
fun Player.hasMediaItemsAsState() = mediaItemCountAsState() > 0

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun Player.playbackSpeedAsState(): Float {
    val flow = remember(this) {
        getPlaybackSpeedAsFlow()
    }
    return flow.collectAsState(initial = getPlaybackSpeed()).value
}

/**
 * Current media metadata [Player.getMediaMetadata]
 */
@Composable
fun Player.currentMediaMetadataAsState(): MediaMetadata {
    val flow = remember(this) {
        currentMediaMetadataAsFlow()
    }
    return flow.collectAsState(initial = mediaMetadata).value
}

/**
 * Current media item index as state [Player.getCurrentMediaItem]
 */
@Composable
fun Player.currentMediaItemIndexAsState(): Int {
    val flow = remember(this) {
        getCurrentMediaItemIndexAsFlow()
    }
    return flow.collectAsState(initial = currentMediaItemIndex).value
}

/**
 * Get current media items as state [Player.getCurrentMediaItems]
 */
@Composable
fun Player.getCurrentMediaItemsAsState(): List<MediaItem> {
    val flow = remember(this) {
        getCurrentMediaItemsAsFlow()
    }
    return flow.collectAsState(initial = getCurrentMediaItems()).value
}

/**
 * Video size as state [Player.getVideoSize]
 */
@Composable
fun Player.videoSizeAsState(): VideoSize {
    val flow = remember(this) {
        videoSizeAsFlow()
    }
    return flow.collectAsState(initial = videoSize).value
}

/**
 * Get aspect ratio as state computed from [Player.getVideoSize]
 *
 * @param defaultAspectRatio The aspect ratio when video size is unknown or for audio content.
 */
@Composable
fun Player.getAspectRatioAsState(defaultAspectRatio: Float): Float {
    val flow = remember(this, defaultAspectRatio) {
        getAspectRatioAsFlow(defaultAspectRatio = defaultAspectRatio)
    }
    return flow.collectAsState(initial = defaultAspectRatio).value
}
