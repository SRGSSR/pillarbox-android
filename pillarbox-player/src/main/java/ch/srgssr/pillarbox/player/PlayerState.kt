/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.player

import androidx.media3.common.Player

/**
 * PlayerState provides Flow's to receive [Player] events like playback state or current position.
 *
 * Sample usage with compose :
 *
 *      @Composable
 *      fun DemoPlayer(player:Player) {
 *          val playerState = remember(player) {
 *              PlayerState(player)
 *          }
 *
 *          val isPlaying = playerState.isPlaying.collectAsState(player.isPlaying)
 *          // or
 *          val isPlaying = playerState.isPlaying()
 *      }
 *
 * @property player The Player to observe.
 */
class PlayerState(val player: Player) {

    /**
     * Is playing [Player.isPlaying]
     */
    val isPlaying = player.isPlayingAsFlow()

    /**
     * Duration [Player.getDuration]
     */
    val duration = player.durationAsFlow()

    /**
     * Playback state [Player.getPlaybackState]
     */
    val playbackState = player.playbackStateAsFlow()

    /**
     * Current position and periodic update position [Player.getCurrentPosition]
     */
    val currentPosition = player.currentPositionAsFlow()

    /**
     * PlayerError [Player.getPlayerError]
     */
    val playerError = player.playerErrorAsFlow()

    /**
     * Can seek to next [Player.getAvailableCommands]
     */
    val availableCommands = player.availableCommandsAsFlow()

    /**
     * Shuffle mode enabled [Player.getShuffleModeEnabled]
     */
    val shuffleModeEnabled = player.shuffleModeEnabledAsFlow()

    /**
     * Media item count [Player.getMediaItemCount]
     */
    val mediaItemCount = player.mediaItemCountAsFlow()

    /**
     * Playback speed [Player.getPlaybackSpeed]
     */
    val playbackSpeed = player.getPlaybackSpeedFlow()
}
