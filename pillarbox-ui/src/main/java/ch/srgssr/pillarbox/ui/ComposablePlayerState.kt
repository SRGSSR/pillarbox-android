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
import ch.srgssr.pillarbox.player.StatefulPlayer
import kotlinx.coroutines.CoroutineScope

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun StatefulPlayer.isPlayingAsState(): Boolean = isPlayingFlow.collectAsState().value

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun StatefulPlayer.playbackStateAsState() = playbackStateFlow.collectAsState().value

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun StatefulPlayer.currentPositionAsState() = currentPositionFlow.collectAsState().value

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun StatefulPlayer.durationAsState() = durationFlow.collectAsState().value

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun StatefulPlayer.availableCommandsAsState() = availableCommandsFlow.collectAsState().value

/**
 * Error [Player.getPlayerError]
 */
@Composable
fun StatefulPlayer.playerErrorAsState() = playerErrorFlow.collectAsState().value

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun StatefulPlayer.shuffleModeEnabledAsState() = shuffleModeEnabledFlow.collectAsState().value

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun StatefulPlayer.mediaItemCountAsState() = mediaItemCountFlow.collectAsState().value

/**
 * @return true if [mediaItemCountAsState] > 0
 */
@Composable
fun StatefulPlayer.hasMediaItemsAsState() = mediaItemCountAsState() > 0

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun StatefulPlayer.playbackSpeedAsState() = playbackSpeedFlow.collectAsState().value

/**
 * Create a remember a [StatefulPlayer]
 *
 * @param player the player to create a [StatefulPlayer]
 * @param scope the coroutine scope in which StateFlow sharing is started.
 */
@Composable
fun rememberPlayerState(player: Player, scope: CoroutineScope = rememberCoroutineScope()): StatefulPlayer {
    return remember(player, scope) {
        StatefulPlayer(player, scope)
    }
}
