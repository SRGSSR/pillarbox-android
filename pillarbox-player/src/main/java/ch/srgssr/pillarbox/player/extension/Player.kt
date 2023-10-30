/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Resume playback
 */
fun Player.startPlayback() {
    when (playbackState) {
        Player.STATE_IDLE -> {
            prepare()
        }

        Player.STATE_ENDED -> {
            seekToDefaultPosition()
        }

        else -> {
            // Nothing
        }
    }
    play()
}

/**
 * Get a snapshot of the current media items
 */
fun Player.getCurrentMediaItems(): List<MediaItem> {
    if (mediaItemCount == 0) {
        return emptyList()
    }
    val count = mediaItemCount
    return ArrayList<MediaItem>(count).apply {
        for (i in 0 until count) {
            add(getMediaItemAt(i))
        }
    }
}

/**
 * Get playback speed
 *
 * @return [Player.getPlaybackParameters] speed
 */
fun Player.getPlaybackSpeed(): Float {
    return playbackParameters.speed
}

/**
 * Current position percent
 *
 * @return the current position in percent [0,1].
 */
fun Player.currentPositionPercentage(): Float {
    return currentPosition / duration.coerceAtLeast(1).toFloat()
}
