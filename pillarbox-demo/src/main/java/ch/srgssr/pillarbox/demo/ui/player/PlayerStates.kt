/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

/**
 * Remember player as state
 *
 * @param player
 * @return
 */
@Composable
fun rememberPlayerAsState(player: Player): PlayerStates {
    val playerStates = remember {
        PlayerStates(player)
    }
    DisposableEffect(key1 = playerStates, effect = {
        onDispose {
            playerStates.dispose()
        }
    })
    return playerStates
}

/**
 * Player states
 *
 * @property player
 * @constructor Create empty Player states
 */
class PlayerStates(val player: Player) : Player by player {
    private val componentListener = ComponentListener()
    private val _isPlaying = mutableStateOf(player.isPlaying)
    private val _playbackState = mutableStateOf(player.playbackState)
    private val _error = mutableStateOf(player.playerError)
    private val _isLoading = mutableStateOf(player.isLoading)

    /**
     * Is playing [Player.isPlaying]
     */
    val isPlaying: State<Boolean> = _isPlaying

    /**
     * Is loading [Player.isLoading]
     */
    val isLoading: State<Boolean> = _isLoading

    /**
     * Playback state change
     */
    val playbackState: State<Int> = _playbackState

    /**
     * Error
     */
    val error: State<PlaybackException?> = _error

    init {
        player.addListener(componentListener)
    }

    /**
     * Dispose
     *
     */
    fun dispose() {
        player.removeListener(componentListener)
    }

    private inner class ComponentListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = player.isPlaying
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            _isLoading.value = isLoading
        }

        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.value = state
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            _error.value = error
        }
    }
}
