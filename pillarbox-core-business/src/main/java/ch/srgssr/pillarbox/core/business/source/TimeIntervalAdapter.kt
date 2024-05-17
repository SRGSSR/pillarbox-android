/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srg.dataProvider.integrationlayer.data.remote.TimeInterval
import ch.srgssr.pillarbox.player.asset.timeRange.Credit

internal object TimeIntervalAdapter {
    internal fun getCredits(timeIntervals: List<TimeInterval>?): List<Credit> {
        return timeIntervals
            .orEmpty()
            .mapNotNull { it.toCredit() }
    }

    internal fun TimeInterval.toCredit(): Credit? {
        val type = type
        val markIn = markIn
        val markOut = markOut
        return if (type == null || markIn == null || markOut == null) {
            null
        } else {
            when (type) {
                TimeInterval.Type.CLOSING_CREDITS -> Credit.Closing(start = markIn, end = markOut)
                TimeInterval.Type.OPENING_CREDITS -> Credit.Opening(start = markIn, end = markOut)
            }
        }
    }
}
