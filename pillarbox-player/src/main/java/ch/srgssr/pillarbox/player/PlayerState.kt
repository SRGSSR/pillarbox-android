/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive

/**
 * PlayerState provides Flow's to receive [Player] events like playback state or current position.
 * Don't forget to call [PlayerState.dispose] when it is no more needed.
 *
 * Sample with PlayerState integration :
 *
 *      SamplePlayerViewModel(player:Player) : ViewModel(){
 *          val playerState = PlayerState(player)
 *
 *          @Override
 *          fun onCleared(){
 *              playerState.dispose()
 *          }
 *      }
 *
 * Sample usage with compose :
 *
 *      @Composable
 *      fun DemoPlayer(player:Player) {
 *          val playerState = remember(player) {
 *              PlayerState(player)
 *          }
 *          DisposableEffect(states) {
 *              onDispose {
 *                  playerState.dispose()
 *              }
 *          }
 *      }
 *
 * @property player The Player to observe.
 */
open class PlayerState(val player: Player) : PlayerDisposable {
    private val playerListener = PlayerListener()
    private val _isPlaying = MutableStateFlow(player.isPlaying)
    private val _isLoading = MutableStateFlow(player.isLoading)
    private val _duration = MutableStateFlow(player.duration)
    private val _currentPosition = MutableStateFlow(player.currentPosition)
    private val _playbackState = MutableStateFlow(player.playbackState)
    private val _playerError = MutableStateFlow(player.playerError)
    private val _availableCommands = MutableStateFlow(player.availableCommands)

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
     * PlayerError [Player.getPlayerError]
     */
    val playerError: StateFlow<PlaybackException?> = _playerError

    /**
     * Can seek to next [Player.getAvailableCommands]
     */
    val availableCommands: StateFlow<Player.Commands> = _availableCommands

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
            _playerError.value = error
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            _availableCommands.value = availableCommands
        }
    }

    companion object {
        private const val UPDATE_DELAY_DURATION_MS = 1000L
    }
}
