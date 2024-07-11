/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.os.SystemClock
import android.util.Log
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pillarbox event logger
 *
 * @param tag The tag to use for logging
 * @constructor Create empty Pillarbox event logger
 */
class PillarboxEventLogger(private val tag: String = "EventLogger") : EventLogger(tag), PillarboxAnalyticsListener {
    private val startTimeMs: Long = SystemClock.elapsedRealtime()

    override fun onStallChanged(eventTime: EventTime, isStall: Boolean) {
        Log.d(tag, getEventString(eventTime, "Stall changed ", isStall.toString()))
    }

    override fun onTrackingEnabledChanged(eventTime: EventTime, trackingEnabled: Boolean) {
        Log.d(tag, getEventString(eventTime, "TrackingEnabledChanged", trackingEnabled.toString()))
    }

    override fun onSmoothSeekingEnabledChanged(eventTime: EventTime, smoothSeekingEnabled: Boolean) {
        Log.d(tag, getEventString(eventTime, "SmoothSeekingEnabledChanged", smoothSeekingEnabled.toString()))
    }

    override fun onBlockedTimeRangeReached(eventTime: EventTime, blockedTimeRange: BlockedTimeRange) {
        Log.d(tag, getEventString(eventTime, "BlockedTimeRangeReached", blockedTimeRange.toString()))
    }

    override fun onCreditChanged(eventTime: EventTime, credit: Credit?) {
        Log.d(tag, getEventString(eventTime, "CreditChanged", credit.toString()))
    }

    override fun onChapterChanged(eventTime: EventTime, chapter: Chapter?) {
        Log.d(tag, getEventString(eventTime, "ChapterChanged", chapter.toString()))
    }

    private fun getEventString(
        eventTime: EventTime,
        eventName: String,
        eventDescription: String,
    ): String {
        return "$eventName [${getEventTimeString(eventTime)}, $eventDescription]"
    }

    private fun getEventTimeString(eventTime: EventTime): String {
        val mediaPeriodId = eventTime.mediaPeriodId
        var windowPeriodString = "window=" + eventTime.windowIndex
        if (mediaPeriodId != null) {
            windowPeriodString += ", period=" + eventTime.timeline.getIndexOfPeriod(mediaPeriodId.periodUid)
            if (mediaPeriodId.isAd) {
                windowPeriodString += ", adGroup=" + mediaPeriodId.adGroupIndex
                windowPeriodString += ", ad=" + mediaPeriodId.adIndexInAdGroup
            }
        }
        return "eventTime=" + (eventTime.realtimeMs - startTimeMs).milliseconds +
            ", mediaPos=" + eventTime.eventPlaybackPositionMs.milliseconds +
            ", " + windowPeriodString
    }
}
