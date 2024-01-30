/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.Player

/**
 * Can seek to a later position in the current or next MediaItem.
 */
fun Player.Commands.canSeekToNext(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_NEXT)
}

/**
 * Can seek to an earlier position in the current or previous MediaItem.
 */
fun Player.Commands.canSeekToPrevious(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_PREVIOUS)
}

/**
 * Can seek back by a fixed increment into the current MediaItem.
 */
fun Player.Commands.canSeekForward(): Boolean {
    return contains(Player.COMMAND_SEEK_FORWARD)
}

/**
 * Can seek back by a fixed increment into the current MediaItem.
 */
fun Player.Commands.canSeekBack(): Boolean {
    return contains(Player.COMMAND_SEEK_BACK)
}

/**
 * Can seek anywhere into the current MediaItem.
 */
fun Player.Commands.canSeek(): Boolean {
    return contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
}

/**
 * Can start, pause or resume playback.
 */
fun Player.Commands.canPlayPause(): Boolean {
    return contains(Player.COMMAND_PLAY_PAUSE)
}

/**
 * Can get details of the current track selection.
 */
fun Player.Commands.canGetTracks(): Boolean {
    return contains(Player.COMMAND_GET_TRACKS)
}

/**
 * set the player's track selection parameters.
 */
fun Player.Commands.canSetTrackSelectionParameters(): Boolean {
    return contains(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
}

/**
 * Can set the playback speed and pitch.
 */
fun Player.Commands.canSpeedAndPitch(): Boolean {
    return contains(Player.COMMAND_SET_SPEED_AND_PITCH)
}
