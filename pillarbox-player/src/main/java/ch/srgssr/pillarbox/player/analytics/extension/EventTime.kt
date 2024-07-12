/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.extension

import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime

internal fun EventTime.getUidOfPeriod(window: Window = Window()): Any {
    timeline.getWindow(windowIndex, window)
    return timeline.getUidOfPeriod(window.firstPeriodIndex)
}
