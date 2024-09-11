/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

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
         * `onChapterChanged` is called when either:
         * - The player position changes while playing automatically.
         * - The use seeks to a new position.
         * - The playlist changes.
         *
         * @param chapter `null` when the current position is not in a chapter.
         */
        fun onChapterChanged(chapter: Chapter?) {}

        /**
         * On blocked time range reached
         *
         * @param blockedTimeRange The [BlockedTimeRange] reached by the player.
         */
        fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {}

        /**
         * `onCreditChanged` is called when either:
         * - The player position changes while playing automatically.
         * - The use seeks to a new position.
         * - The playlist changes.
         *
         * @param credit `null` when the current position is not in a Credit.
         */
        fun onCreditChanged(credit: Credit?) {}
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

    @Suppress("UndocumentedPublicClass")
    companion object {

        /**
         * Event Blocked Time Range Reached.
         */
        const val EVENT_BLOCKED_TIME_RANGE_REACHED = 100

        /**
         * The current [Chapter] has changed.
         */
        const val EVENT_CHAPTER_CHANGED = 101

        /**
         * The current [Credit] Changed.
         */
        const val EVENT_CREDIT_CHANGED = 102

        /**
         * [trackingEnabled] has changed.
         */
        const val EVENT_TRACKING_ENABLED_CHANGED = 103

        /**
         * [smoothSeekingEnabled] has changed.
         */
        const val EVENT_SMOOTH_SEEKING_ENABLED_CHANGED = 104
    }
}
