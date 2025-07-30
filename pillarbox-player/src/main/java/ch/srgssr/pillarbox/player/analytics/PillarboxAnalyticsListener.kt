/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

/**
 * An interface for receiving analytics events from a [PillarboxPlayer].
 */
interface PillarboxAnalyticsListener : AnalyticsListener {
    /**
     * Called when the smooth seeking enabled state changes.
     *
     * @param eventTime The [EventTime].
     * @param smoothSeekingEnabled Whether smooth seeking is enabled.
     *
     * @see PillarboxPlayer.smoothSeekingEnabled
     */
    fun onSmoothSeekingEnabledChanged(eventTime: EventTime, smoothSeekingEnabled: Boolean) {}

    /**
     * Called when the tracking enabled state changes.
     *
     * @param eventTime The [EventTime].
     * @param trackingEnabled Whether tracking is enabled.
     *
     * @see PillarboxPlayer.trackingEnabled
     */
    fun onTrackingEnabledChanged(eventTime: EventTime, trackingEnabled: Boolean) {}

    /**
     * Called when the current chapter changes. This can occur when either:
     * - The player's position changes naturally during playback.
     * - The user seeks to a new position.
     * - The playlist changes.
     *
     * @param eventTime The [EventTime].
     * @param chapter The active [Chapter], or `null` if the current position is not within a chapter.
     */
    fun onChapterChanged(eventTime: EventTime, chapter: Chapter?) {}

    /**
     * Called when the player reaches a blocked time range.
     *
     * @param eventTime The [EventTime].
     * @param blockedTimeRange The [BlockedTimeRange] that the player has entered.
     */
    fun onBlockedTimeRangeReached(eventTime: EventTime, blockedTimeRange: BlockedTimeRange) {}

    /**
     * Called when the current credit changes. This can occur when either:
     * - The player's position changes naturally during playback.
     * - The user seeks to a new position.
     * - The playlist changes.
     *
     * @param eventTime The [EventTime].
     * @param credit The active [Credit], or `null` if the current position is not within a credit.
     */
    fun onCreditChanged(eventTime: EventTime, credit: Credit?) {}

    /**
     * Called when the player's stall state changes.
     *
     * A stall occurs when the player is buffering ([Player.STATE_BUFFERING]) after previously being in a ready state ([Player.STATE_READY]) during
     * playback, and this change was not initiated by a user interaction (e.g., seeking).
     *
     * @param eventTime The [EventTime].
     * @param isStall Whether the player is currently stalling.
     */
    fun onStallChanged(eventTime: EventTime, isStall: Boolean) {}

    /**
     * Called when the player's metadata changes.
     */
    fun onPillarboxMetaDataChanged(eventTime: EventTime, pillarboxMetadata: PillarboxMetadata) {}

    companion object {
        /**
         * @see [PillarboxPlayer.EVENT_BLOCKED_TIME_RANGE_REACHED]
         */
        const val EVENT_BLOCKED_TIME_RANGE_REACHED = PillarboxPlayer.EVENT_BLOCKED_TIME_RANGE_REACHED

        /**
         * @see [PillarboxPlayer.EVENT_CREDIT_CHANGED]
         */
        const val EVENT_CREDIT_CHANGED = PillarboxPlayer.EVENT_CREDIT_CHANGED

        /**
         * @see [PillarboxPlayer.EVENT_CHAPTER_CHANGED]
         */
        const val EVENT_CHAPTER_CHANGED = PillarboxPlayer.EVENT_CHAPTER_CHANGED

        /**
         * @see [PillarboxPlayer.EVENT_TRACKING_ENABLED_CHANGED]
         */
        const val EVENT_TRACKING_ENABLED_CHANGED = PillarboxPlayer.EVENT_TRACKING_ENABLED_CHANGED

        /**
         * @see [PillarboxPlayer.EVENT_SMOOTH_SEEKING_ENABLED_CHANGED]
         */
        const val EVENT_SMOOTH_SEEKING_ENABLED_CHANGED = PillarboxPlayer.EVENT_SMOOTH_SEEKING_ENABLED_CHANGED

        /**
         * Event Stall Changed
         */
        const val EVENT_STALL_CHANGED = 200

        /**
         * Event Stall Changed
         */
        const val EVENT_PILLARBOX_META_DATA_CHANGED = PillarboxPlayer.EVENT_PILLARBOX_META_DATA_CHANGED
    }
}
