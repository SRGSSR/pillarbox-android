/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.viewmodel.PlayerDisposable
import ch.srgssr.pillarbox.player.viewmodel.PlayerViewModel

/**
 * Is playing [Player.isPlaying]
 */
@Composable
fun PlayerViewModel.isPlaying(): Boolean = isPlaying.collectAsState().value

/**
 * Is playing [Player.isLoading]
 */
@Composable
fun PlayerViewModel.isLoading() = isLoading.collectAsState().value

/**
 * Is playing [Player.getPlaybackState]
 */
@Composable
fun PlayerViewModel.playbackState() = playbackState.collectAsState().value

/**
 * Is playing [Player.getCurrentPosition]
 */
@Composable
fun PlayerViewModel.currentPosition() = currentPosition.collectAsState(player.currentPosition).value

/**
 * Is playing [Player.getDuration]
 */
@Composable
fun PlayerViewModel.duration() = duration.collectAsState().value

/**
 * Is playing [Player.isCurrentMediaItemSeekable]
 */
@Composable
fun PlayerViewModel.isCurrentMediaItemSeekable() = isCurrentMediaItemSeekable.collectAsState(false).value

/**
 * Create a remember a PlayerViewModel
 *
 * @param player Player to create a PlayerViewModel
 */
@Composable
fun rememberPlayerViewModel(player: Player): PlayerViewModel {
    return rememberPlayerViewModel(player = player) { PlayerViewModel(it) }
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
fun <T : PlayerDisposable, P : Player> rememberPlayerViewModel(player: P, factory: (P) -> T): T {
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
