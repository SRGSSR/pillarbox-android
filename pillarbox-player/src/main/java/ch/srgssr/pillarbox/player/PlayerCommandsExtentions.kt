/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player

/**
 * Can seek to next
 */
fun Player.Commands.canSeekToNext(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_NEXT)
}

/**
 * Can seek to previous
 */
fun Player.Commands.canSeekToPrevious(): Boolean {
    return contains(Player.COMMAND_SEEK_TO_PREVIOUS)
}

/**
 * Can seek forward
 */
fun Player.Commands.canSeekForward(): Boolean {
    return contains(Player.COMMAND_SEEK_FORWARD)
}

/**
 * Can seek back
 */
fun Player.Commands.canSeekBack(): Boolean {
    return contains(Player.COMMAND_SEEK_BACK)
}

/**
 * Can seek in current MediaItem
 */
fun Player.Commands.canSeek(): Boolean {
    return contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
}

/**
 * Can play and pause
 */
fun Player.Commands.canPlayPause(): Boolean {
    return contains(Player.COMMAND_PLAY_PAUSE)
}
