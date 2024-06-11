/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import androidx.media3.common.PlaybackException
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

    override fun onTrackingEnabledChanged(eventTime: EventTime, trackingEnabled: Boolean) {
        Log.d(tag, getEventString(eventTime, "TrackingEnabledChanged", trackingEnabled.toString()))
    }

    override fun onSmoothSeekingEnabledChanged(eventTime: EventTime, smoothSeekingEnabled: Boolean) {
        Log.d(tag, getEventString(eventTime, "SmoothSeekingEnabled", smoothSeekingEnabled.toString()))
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
        eventDescription: String? = null,
        throwable: Throwable? = null
    ): String {
        var eventString = eventName + " [" + getEventTimeString(eventTime)
        if (throwable is PlaybackException) {
            eventString += ", errorCode=" + throwable.errorCodeName
        }
        if (eventDescription != null) {
            eventString += ", $eventDescription"
        }
        val throwableString = androidx.media3.common.util.Log.getThrowableString(throwable)
        if (!TextUtils.isEmpty(throwableString)) {
            eventString += """
  ${throwableString!!.replace("\n", "\n  ")}
"""
        }
        eventString += "]"
        return eventString
    }

    private fun getEventTimeString(eventTime: EventTime): String {
        var windowPeriodString = "window=" + eventTime.windowIndex
        if (eventTime.mediaPeriodId != null) {
            windowPeriodString +=
                ", period=" + eventTime.timeline.getIndexOfPeriod(eventTime.mediaPeriodId!!.periodUid)
            if (eventTime.mediaPeriodId!!.isAd) {
                windowPeriodString += ", adGroup=" + eventTime.mediaPeriodId!!.adGroupIndex
                windowPeriodString += ", ad=" + eventTime.mediaPeriodId!!.adIndexInAdGroup
            }
        }
        return (
            "eventTime=" +
                (eventTime.realtimeMs - startTimeMs).milliseconds +
                ", mediaPos=" +
                eventTime.eventPlaybackPositionMs.milliseconds +
                ", " +
                windowPeriodString
            )
    }
}
