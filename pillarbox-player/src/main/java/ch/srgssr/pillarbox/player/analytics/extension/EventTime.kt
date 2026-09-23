/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.extension

import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.analytics.WindowUid
import ch.srgssr.pillarbox.player.analytics.getWindowUid

internal fun EventTime.getUidOfPeriod(window: Window): Any {
    timeline.getWindow(windowIndex, window)
    return timeline.getUidOfPeriod(window.firstPeriodIndex)
}

/**
 * Get the window uid
 */
fun EventTime.getWindowUid(window: Window = Window()): WindowUid {
    timeline.getWindow(windowIndex, window)
    return window.getWindowUid()
}
