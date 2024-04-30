/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.asset.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.asset.SkipableTimeRange

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
         * `onCurrentChapterChanged` is called when either:
         * - The player position changes while playing automatically.
         * - The use seeks to a new position.
         * - The playlist changes.
         *
         * @param chapter `null` when the current position is not in a chapter.
         */
        fun onCurrentChapterChanged(chapter: Chapter?) {}

        /**
         * On blocked time range reached
         *
         * @param blockedTimeRange The [BlockedTimeRange] reached by the player.
         */
        fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {}

        /**
         * `onSkipableTimeRangeChanged` is called when either:
         * - The player position changes while playing automatically.
         * - The use seeks to a new position.
         * - The playlist changes.
         *
         * @param timeRange `null` when the current position is not in a time interval.
         */
        fun onSkipableTimeRangeChanged(timeRange: SkipableTimeRange?) {}
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
