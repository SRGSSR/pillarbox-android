/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.player.viewmodel

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive

/**
 * @property player
 * @constructor Create empty Player states
 */
open class PlayerViewModel(val player: Player) : PlayerDisposable {
    private val playerListener = PlayerListener()
    private val _isPlaying = MutableStateFlow(player.isPlaying)
    private val _isLoading = MutableStateFlow(player.isLoading)
    private val _duration = MutableStateFlow(player.duration)
    private val _isContentSeekable = MutableStateFlow(player.isCurrentMediaItemSeekable)
    private val _currentPosition = MutableStateFlow(player.currentPosition)
    private val _playbackState = MutableStateFlow(player.playbackState)
    private val _error = MutableStateFlow(player.playerError)

    /**
     * Ticker emits only when [player] is playing every [UPDATE_DELAY_DURATION_MS].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    protected val tickerWhilePlaying = _isPlaying.transformLatest {
        while (currentCoroutineContext().isActive && it) {
            emit(Unit)
            delay(UPDATE_DELAY_DURATION_MS)
        }
    }

    private val periodicCurrentPosition = tickerWhilePlaying.map {
        player.currentPosition
    }

    /**
     * Is loading [Player.isLoading]
     */
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Is playing [Player.isPlaying]
     */
    val isPlaying: StateFlow<Boolean> = _isPlaying

    /**
     * Playback state [Player.getPlaybackState]
     */
    val playbackState: StateFlow<Int> = _playbackState

    /**
     * Duration [Player.getDuration]
     */
    val duration: StateFlow<Long> = _duration

    /**
     * Current position and periodic update position [Player.getCurrentPosition]
     */
    val currentPosition = merge(_currentPosition, periodicCurrentPosition)

    /**
     * Is current MediaItem seekable [Player.isCurrentMediaItemSeekable]
     */
    val isCurrentMediaItemSeekable = _isContentSeekable.combine(duration) { seekable, duration ->
        seekable && duration > 0
    }

    /**
     * Error [Player.getPlayerError]
     */
    val error: StateFlow<PlaybackException?> = _error

    init {
        player.addListener(playerListener)
    }

    override fun dispose() {
        player.removeListener(playerListener)
    }

    private inner class PlayerListener : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            _isLoading.value = isLoading
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            _duration.value = player.duration
            _isContentSeekable.value = player.isCurrentMediaItemSeekable
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playbackState.value = playbackState
            if (playbackState == Player.STATE_READY) {
                _duration.value = player.duration
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                _currentPosition.value = player.currentPosition
            }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            _error.value = error
        }
    }

    companion object {
        private const val UPDATE_DELAY_DURATION_MS = 1000L
    }
}
