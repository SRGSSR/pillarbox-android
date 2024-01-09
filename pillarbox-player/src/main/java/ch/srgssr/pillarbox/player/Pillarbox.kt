/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters

/**
 * Pillarbox [ExoPlayer] interface extension.
 */
interface Pillarbox : ExoPlayer {

    /**
     * Listener
     */
    interface Listener : Player.Listener {
        /**
         * On smooth seeking enabled changed
         *
         * @param smoothSeekingEnabled The new value of [smoothSeekingEnabled]
         */
        fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean)
    }

    /**
     * Smooth seeking enabled
     *
     * When [smoothSeekingEnabled] is enabled, next seek event is send only after the current is done.
     *
     * To have the best result it is important to
     * 1) Pause the player while seeking
     * 2) Set the [ExoPlayer.setSeekParameters] to [SeekParameters.CLOSEST_SYNC].
     */
    var smoothSeekingEnabled: Boolean
}
