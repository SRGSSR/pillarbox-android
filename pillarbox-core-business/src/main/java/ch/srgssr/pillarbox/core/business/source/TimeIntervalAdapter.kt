/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeIntervalType
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

internal object TimeIntervalAdapter {
    internal fun getCredits(timeIntervals: List<TimeInterval>?): List<Credit> {
        return timeIntervals
            .orEmpty()
            .mapNotNull { it.toSkipableTimeInterval() }
    }

    internal fun TimeInterval.toSkipableTimeInterval(): Credit? {
        return if (type == null || markIn == null || markOut == null) {
            null
        } else {
            when (type) {
                TimeIntervalType.CLOSING_CREDITS -> Credit.Closing(start = markIn, end = markOut)
                TimeIntervalType.OPENING_CREDITS -> Credit.Opening(start = markIn, end = markOut)
            }
        }
    }
}
