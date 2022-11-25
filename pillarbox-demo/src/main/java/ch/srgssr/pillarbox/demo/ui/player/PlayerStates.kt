/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

/**
 * Remember player as state
 *
 * @param player initial value
 */
@Composable
fun rememberPlayerAsState(player: Player): PlayerStates {
    val playerStates = remember {
        PlayerStates(player)
    }
    playerStates.player = player
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
 * @param initialValue
 */
class PlayerStates(initialValue: Player) {
    /**
     * Player currently bind to the [PlayerStates]
     */
    var player: Player = initialValue
        set(value) {
            if (field != value) {
                field.removeListener(componentListener)
                field = value
                resetStates()
                field.addListener(componentListener)
            }
        }
    private val componentListener = ComponentListener()
    private val _isPlaying = mutableStateOf(player.isPlaying)
    private val _playbackState = mutableStateOf(player.playbackState)
    private val _error = mutableStateOf(player.playerError)
    private val _isLoading = mutableStateOf(player.isLoading)
    private val _duration = mutableStateOf(getSafeDuration())
    private val _isContentSeekable = mutableStateOf(player.isCurrentMediaItemSeekable)
    private val _playerProgressPercent = MutableStateFlow(getProgressPercent())
    private val _periodicProgressPercent = flow {
        while (true) {
            if (player.isPlaying) {
                emit(getProgressPercent())
            }
            delay(1000)
        }
    }

    /**
     * Progress percentage of the current media
     */
    val progressPercentage = merge(_playerProgressPercent, _periodicProgressPercent)

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

    /**
     * Duration
     */
    val duration: State<Long> = _duration

    /**
     * Duration
     */
    val isContentSeekable: State<Boolean> = _isContentSeekable

    init {
        Log.d("Coucou", "PlayerStates init $player")
        player.addListener(componentListener)
    }

    private fun resetStates() {
        _duration.value = getSafeDuration()
        _isContentSeekable.value = player.isCurrentMediaItemSeekable
        _isLoading.value = player.isLoading
        _isPlaying.value = player.isPlaying
        _error.value = player.playerError
        _playbackState.value = player.playbackState
        _playerProgressPercent.value = getProgressPercent()
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
            if (state == Player.STATE_READY) {
                _duration.value = getSafeDuration()
            }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            _error.value = error
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            _duration.value = getSafeDuration()
            _isContentSeekable.value = player.isCurrentMediaItemSeekable
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                _playerProgressPercent.value = getProgressPercent()
            }
        }
    }

    private fun getSafeDuration(): Long {
        return if (player.duration == C.TIME_UNSET) 1L else player.duration
    }

    private fun getSafePosition(): Long {
        return if (player.currentPosition == C.TIME_UNSET) 0L else player.currentPosition
    }

    private fun getProgressPercent(): Float {
        return getSafePosition() / getSafeDuration().toFloat()
    }
}
