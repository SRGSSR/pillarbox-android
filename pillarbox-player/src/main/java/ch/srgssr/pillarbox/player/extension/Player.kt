/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

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
