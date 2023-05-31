/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.PlayerDisposable
import ch.srgssr.pillarbox.player.PlayerState

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun PlayerState.isPlaying(): Boolean = isPlaying.collectAsState().value

/**
 * Is playing [Player.isLoading]
 */
@Composable
fun PlayerState.isLoading() = isLoading.collectAsState().value

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun PlayerState.playbackState() = playbackState.collectAsState().value

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun PlayerState.currentPosition() = currentPosition.collectAsState(player.currentPosition).value

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun PlayerState.duration() = duration.collectAsState().value

/**
 * Available commands [Player.getAvailableCommands]
 */
@Composable
fun PlayerState.availableCommands() = availableCommands.collectAsState().value

/**
 * Error [Player.getPlayerError]
 */
@Composable
fun PlayerState.playerError() = playerError.collectAsState().value

/**
 * Shuffle mode enabled [Player.getShuffleModeEnabled]
 */
@Composable
fun PlayerState.shuffleModeEnabled() = shuffleModeEnabled.collectAsState().value

/**
 * Media item count [Player.getMediaItemCount]
 */
@Composable
fun PlayerState.mediaItemCount() = mediaItemCount.collectAsState().value

/**
 * @return true if [mediaItemCount] > 0
 */
@Composable
fun PlayerState.hasMediaItems() = mediaItemCount() > 0

/**
 * Playback speed [Player.getPlaybackParameters]
 */
@Composable
fun PlayerState.playbackSpeed() = playbackSpeed.collectAsState().value

/**
 * Create a remember a [PlayerState]
 *
 * @param player Player to create a [PlayerState]
 */
@Composable
fun rememberPlayerState(player: Player): PlayerState {
    return rememberPlayerDisposable(player = player) { PlayerState(it) }
}

/**
 * Create a remember a T that expend [PlayerDisposable]
 *
 * @param T T the [PlayerDisposable] class.
 * @param P P the [Player] class.
 * @param player The player to use with [factory]
 * @param factory The factory to create a instance of T from P.
 */
@Composable
fun <T : PlayerDisposable, P : Player> rememberPlayerDisposable(player: P, factory: (P) -> T): T {
    val states = remember(player) {
        factory(player)
    }
    DisposableEffect(states) {
        onDispose {
            states.dispose()
        }
    }
    return states
}
