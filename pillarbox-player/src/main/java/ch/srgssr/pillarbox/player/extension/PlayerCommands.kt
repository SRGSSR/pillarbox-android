/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("TooManyFunctions")

package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Checks if the player can seek to a later position in the current or next [MediaItem].
 *
 * @return Whether the player can seek to a later position.
 */
fun Player.Commands.canSeekToNext(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_NEXT)
}

/**
 * Checks if the player can seek to an earlier position in the current or previous [MediaItem].
 *
 * @return Whether the player can seek to an earlier position.
 */
fun Player.Commands.canSeekToPrevious(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_PREVIOUS)
}

/**
 * Checks if the player can seek forward by a fixed increment in the current [MediaItem].
 *
 * @return Whether the player supports seeking forward.
 */
fun Player.Commands.canSeekForward(): Boolean {
    return contains(Player.COMMAND_SEEK_FORWARD)
}

/**
 * Checks if the player can seek back by a fixed increment in the current [MediaItem].
 *
 * @return Whether the player supports seeking back.
 */
fun Player.Commands.canSeekBack(): Boolean {
    return contains(Player.COMMAND_SEEK_BACK)
}

/**
 * Checks if the player can seek in the current [MediaItem].
 *
 * @return Whether the player supports seeking.
 */
fun Player.Commands.canSeek(): Boolean {
    return contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
}

/**
 * Checks if the player can play/pause/resume the current [MediaItem].
 *
 * @return Whether the player can play/pause/resume.
 */
fun Player.Commands.canPlayPause(): Boolean {
    return contains(Player.COMMAND_PLAY_PAUSE)
}

/**
 * Checks if the player can get the tracks in the current [MediaItem].
 *
 * @return Whether the player can get the tracks.
 */
fun Player.Commands.canGetTracks(): Boolean {
    return contains(Player.COMMAND_GET_TRACKS)
}

/**
 * Checks if the player can set track selection parameters.
 *
 * @return Whether the player can set track selection parameters.
 */
fun Player.Commands.canSetTrackSelectionParameters(): Boolean {
    return contains(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
}

/**
 * Checks if the player can set the playback speed and pitch of the current [MediaItem].
 *
 * @return Whether the player can set the playback speed and pitch.
 */
fun Player.Commands.canSpeedAndPitch(): Boolean {
    return contains(Player.COMMAND_SET_SPEED_AND_PITCH)
}

/**
 * Checks if the player can set the shuffle mode of the current [MediaItem].
 *
 * @return Whether the player can set the shuffle mode.
 */
fun Player.Commands.canSetShuffleMode(): Boolean {
    return contains(Player.COMMAND_SET_SHUFFLE_MODE)
}

/**
 * Checks if the player can set the repeat mode of the current [MediaItem].
 *
 * @return Whether the player can set the repeat mode.
 */
fun Player.Commands.canSetRepeatMode(): Boolean {
    return contains(Player.COMMAND_SET_REPEAT_MODE)
}
