/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.asset.Chapter

/**
 * Pillarbox [Player] interface extension.
 */
interface PillarboxPlayer : Player {
    /**
     * Listener
     */
    interface Listener : Player.Listener {
        /**
         * On smooth seeking enabled changed
         *
         * @param smoothSeekingEnabled The new value of [PillarboxPlayer.smoothSeekingEnabled]
         */
        fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean) {}

        /**
         * On tracking enabled changed
         *
         * @param trackingEnabled The new value of [PillarboxPlayer.trackingEnabled]
         */
        fun onTrackingEnabledChanged(trackingEnabled: Boolean) {}

        /**
         * On current chapter changed is called when either:
         * - Player position changed during playing automatically.
         * - Use seek at a position.
         * - Playlist changes.
         *
         * @param chapter null when current position is not in a chapter.
         */
        fun onCurrentChapterChanged(chapter: Chapter?) {}

        /**
         * On block interval reached
         *
         * @param blockedInterval The [BlockedInterval] reach by the player.
         */
        fun onBlockIntervalReached(blockedInterval: BlockedInterval) {}
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
     * Enable or disable MediaItem tracking
     */
    var trackingEnabled: Boolean
}
