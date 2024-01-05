/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

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
     */
    var smoothSeekingEnabled: Boolean
}
