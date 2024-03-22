/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.asset.TimeInterval

/**
 * Pillarbox [ExoPlayer] interface extension.
 */
interface PillarboxExoPlayer : ExoPlayer {

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

        /**
         * Event triggered when entering a [time interval][TimeInterval].
         *
         * @param timeInterval The [TimeInterval]
         */
        fun onEnterTimeInterval(timeInterval: TimeInterval)

        /**
         * Event triggered when exiting a [time interval][TimeInterval].
         *
         * @param timeInterval The [TimeInterval]
         */
        fun onExitTimeInterval(timeInterval: TimeInterval)
    }

    /**
     * Smooth seeking enabled
     *
     * When [smoothSeekingEnabled] is true, next seek events is send only after the current is done.
     *
     * To have the best result it is important to
     * 1) Pause the player while seeking.
     * 2) Set the [ExoPlayer.setSeekParameters] to [SeekParameters.CLOSEST_SYNC].
     */
    var smoothSeekingEnabled: Boolean

    /**
     * Get the list of listeners specific to `PillarboxExoPlayer`.
     */
    fun getPillarboxListeners(): List<Listener>

    /**
     * Get the [time intervals][TimeInterval] at the specified [position][positionMs].
     */
    fun getTimeIntervalsAt(positionMs: Long): List<TimeInterval>
}
