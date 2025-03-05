/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.common.util.ListenerSet.Event
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

/**
 * Collects and dispatches analytics events for Pillarbox player.
 *
 * @param clock The [Clock] used to generate timestamps.
 */
class PillarboxAnalyticsCollector(
    clock: Clock = Clock.DEFAULT,
) : DefaultAnalyticsCollector(clock), PillarboxPlayer.Listener, StallDetector.Listener {

    private val stallDetector = StallDetector()

    internal val sessionManager = PlaybackSessionManager()

    internal val metricsCollector: MetricsCollector = MetricsCollector(sessionManager)

    init {
        addListener(sessionManager)
        addListener(metricsCollector)
        addListener(stallDetector)
        stallDetector.addListener(this)
    }

    override fun setPlayer(player: Player, looper: Looper) {
        super.setPlayer(player, looper)
    }

    override fun release() {
        stallDetector.removeListener(this)
        super.release()
    }

    override fun onStallChanged(isStall: Boolean) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_STALL_CHANGED) { listener ->
            listener.onStallChanged(eventTime, isStall)
        }
    }

    override fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_SMOOTH_SEEKING_ENABLED_CHANGED) { listener ->
            listener.onSmoothSeekingEnabledChanged(eventTime, smoothSeekingEnabled)
        }
    }

    override fun onTrackingEnabledChanged(trackingEnabled: Boolean) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_TRACKING_ENABLED_CHANGED) { listener ->
            listener.onTrackingEnabledChanged(eventTime, trackingEnabled)
        }
    }

    override fun onChapterChanged(chapter: Chapter?) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_CHAPTER_CHANGED) { listener ->
            listener.onChapterChanged(eventTime, chapter)
        }
    }

    override fun onCreditChanged(credit: Credit?) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_CREDIT_CHANGED) { listener ->
            listener.onCreditChanged(eventTime, credit)
        }
    }

    override fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {
        val eventTime = generateCurrentPlayerMediaPeriodEventTime()

        sendEventPillarbox(eventTime, PillarboxAnalyticsListener.EVENT_BLOCKED_TIME_RANGE_REACHED) { listener ->
            listener.onBlockedTimeRangeReached(eventTime, blockedTimeRange)
        }
    }

    private fun sendEventPillarbox(eventTime: EventTime, eventFlag: Int, event: Event<PillarboxAnalyticsListener>) {
        sendEvent(eventTime, eventFlag) { listener ->
            if (listener is PillarboxAnalyticsListener) event.invoke(listener)
        }
    }
}
