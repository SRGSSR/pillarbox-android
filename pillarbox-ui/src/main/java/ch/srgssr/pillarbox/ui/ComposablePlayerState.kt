/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerState
import kotlinx.coroutines.CoroutineScope

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun PlayerState.isPlayingAsState(): Boolean = isPlayingFlow.collectAsState().value

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun PlayerState.playbackStateAsState() = playbackStateFlow.collectAsState().value

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun PlayerState.currentPositionAsState() = currentPositionFlow.collectAsState().value

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun PlayerState.durationAsState() = durationFlow.collectAsState().value

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun PlayerState.availableCommandsAsState() = availableCommandsFlow.collectAsState().value

/**
 * Error [Player.getPlayerError]
 */
@Composable
fun PlayerState.playerErrorAsState() = playerErrorFlow.collectAsState().value

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun PlayerState.shuffleModeEnabledAsState() = shuffleModeEnabledFlow.collectAsState().value

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun PlayerState.mediaItemCountAsState() = mediaItemCountFlow.collectAsState().value

/**
 * @return true if [mediaItemCountAsState] > 0
 */
@Composable
fun PlayerState.hasMediaItemsAsState() = mediaItemCountAsState() > 0

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun PlayerState.playbackSpeedAsState() = playbackSpeedFlow.collectAsState().value

/**
 * Create a remember a [PlayerState]
 *
 * @param player the player to create a [PlayerState]
 * @param scope the coroutine scope in which StateFlow sharing is started.
 */
@Composable
fun rememberPlayerState(player: Player, scope: CoroutineScope = rememberCoroutineScope()): PlayerState {
    return remember(player, scope) {
        PlayerState(player, scope)
    }
}
