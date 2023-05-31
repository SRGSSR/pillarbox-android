/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.player.getPlaybackSpeed

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun PlayerState.isPlaying(): Boolean = isPlaying.collectAsState(player.isPlaying).value

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun PlayerState.playbackState() = playbackState.collectAsState(player.playbackState).value

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun PlayerState.currentPosition() = currentPosition.collectAsState(player.currentPosition).value

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun PlayerState.duration() = duration.collectAsState(player.duration).value

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun PlayerState.availableCommands() = availableCommands.collectAsState(player.availableCommands).value

/**
 * Error [Player.getPlayerError]
 */
@Composable
fun PlayerState.playerError() = playerError.collectAsState(player.playerError).value

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun PlayerState.shuffleModeEnabled() = shuffleModeEnabled.collectAsState(player.shuffleModeEnabled).value

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun PlayerState.mediaItemCount() = mediaItemCount.collectAsState(player.mediaItemCount).value

/**
 * @return true if [mediaItemCount] > 0
 */
@Composable
fun PlayerState.hasMediaItems() = mediaItemCount() > 0

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun PlayerState.playbackSpeed() = playbackSpeed.collectAsState(player.getPlaybackSpeed()).value

/**
 * Create a remember a [PlayerState]
 *
 * @param player Player to create a [PlayerState]
 */
@Composable
fun rememberPlayerState(player: Player): PlayerState {
    return remember(player) {
        PlayerState(player)
    }
}
