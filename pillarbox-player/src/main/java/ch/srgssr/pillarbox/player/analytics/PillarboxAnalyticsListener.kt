/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

/**
 * Pillarbox analytics listener
 *
 * @see [AnalyticsListener]
 */
interface PillarboxAnalyticsListener : AnalyticsListener {
    /**
     * On smooth seeking enabled changed
     *
     * @param eventTime The [EventTime].
     * @param smoothSeekingEnabled The new value of [PillarboxPlayer.smoothSeekingEnabled]
     */
    fun onSmoothSeekingEnabledChanged(eventTime: EventTime, smoothSeekingEnabled: Boolean) {}

    /**
     * On tracking enabled changed
     *
     * @param eventTime The [EventTime].
     * @param trackingEnabled The new value of [PillarboxPlayer.trackingEnabled]
     */
    fun onTrackingEnabledChanged(eventTime: EventTime, trackingEnabled: Boolean) {}

    /**
     * `onChapterChanged` is called when either:
     * - The player position changes while playing automatically.
     * - The use seeks to a new position.
     * - The playlist changes.
     *
     * @param eventTime The [EventTime].
     * @param chapter `null` when the current position is not in a chapter.
     */
    fun onChapterChanged(eventTime: EventTime, chapter: Chapter?) {}

    /**
     * On blocked time range reached
     *
     * @param eventTime The [EventTime].
     * @param blockedTimeRange The [BlockedTimeRange] reached by the player.
     */
    fun onBlockedTimeRangeReached(eventTime: EventTime, blockedTimeRange: BlockedTimeRange) {}

    /**
     * `onCreditChanged` is called when either:
     * - The player position changes while playing automatically.
     * - The use seeks to a new position.
     * - The playlist changes.
     *
     * @param eventTime The [EventTime]
     * @param credit `null` when the current position is not in a Credit.
     */
    fun onCreditChanged(eventTime: EventTime, credit: Credit?) {}

    @Suppress("UndocumentedPublicClass")
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
    }
}
