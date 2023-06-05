/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * PlayerState provides Flow's to receive [Player] events like playback state or current position.
 *
 * Sample usage with compose :
 *
 *      @Composable
 *      fun DemoPlayer(player:Player) {
 *          val coroutineScope = rememberCoroutineScope()
 *          val playerState = remember(player) {
 *              PlayerState(player, coroutineScope)
 *          }
 *
 *          val isPlaying = playerState.isPlayingFlow.collectAsState()
 *          // or
 *          val isPlaying = playerState.isPlaying()
 *      }
 *
 * @property player the Player to observe.
 * @param scope the coroutine scope in which sharing is started.
 */
class StatefulPlayer(private val player: Player, scope: CoroutineScope) : Player by player {

    /**
     * Is playing [Player.isPlaying]
     */
    val isPlayingState by lazy {
        isPlayingAsFlow().stateIn(scope = scope, initialValue = player.isPlaying, started = SharingStarted.WhileSubscribed())
    }

    /**
     * Duration [Player.getDuration]
     */
    val durationState by lazy {
        durationAsFlow().stateIn(scope = scope, initialValue = player.duration, started = SharingStarted.WhileSubscribed())
    }

    /**
     * Playback state [Player.getPlaybackState]
     */
    val playbackStateAsState by lazy {
        playbackStateAsFlow().stateIn(
            scope = scope, initialValue = player.playbackState, started = SharingStarted.WhileSubscribed()
        )
    }

    /**
     * Current position and periodic update position [Player.getCurrentPosition]
     */
    val currentPositionState by lazy {
        currentPositionAsFlow().stateIn(
            scope = scope, initialValue = player.currentPosition, started = SharingStarted.WhileSubscribed()
        )
    }

    /**
     * PlayerError [Player.getPlayerError]
     */
    val playerErrorState by lazy {
        playerErrorAsFlow().stateIn(
            scope = scope, initialValue = player.playerError, started = SharingStarted.WhileSubscribed()
        )
    }

    /**
     * Can seek to next [Player.getAvailableCommands]
     */
    val availableCommandsState by lazy {
        availableCommandsAsFlow().stateIn(
            scope = scope, initialValue = player.availableCommands,
            started = SharingStarted
                .WhileSubscribed()
        )
    }

    /**
     * Shuffle mode enabled [Player.getShuffleModeEnabled]
     */
    val shuffleModeEnabledState by lazy {
        shuffleModeEnabledAsFlow().stateIn(
            scope = scope, initialValue = player.shuffleModeEnabled,
            started = SharingStarted
                .WhileSubscribed()
        )
    }

    /**
     * Media item count [Player.getMediaItemCount]
     */
    val mediaItemCountState by lazy {
        mediaItemCountAsFlow().stateIn(
            scope = scope, initialValue = player.mediaItemCount,
            started = SharingStarted
                .WhileSubscribed()
        )
    }

    /**
     * Playback speed [Player.getPlaybackSpeed]
     */
    val playbackSpeedState by lazy {
        getPlaybackSpeedAsFlow().stateIn(
            scope = scope, initialValue = player.getPlaybackSpeed(),
            started = SharingStarted
                .WhileSubscribed()
        )
    }
}
