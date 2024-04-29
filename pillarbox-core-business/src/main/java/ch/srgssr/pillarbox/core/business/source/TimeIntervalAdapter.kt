/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.player.asset.SkipableTimeInterval

internal object TimeIntervalAdapter {
    internal fun getTimeIntervals(timeIntervals: List<TimeInterval>?): List<SkipableTimeInterval> {
        return timeIntervals
            .orEmpty()
            .mapNotNull { it.toSkipableTimeInterval() }
    }

    internal fun TimeInterval.toSkipableTimeInterval(): SkipableTimeInterval? {
        return if (type == null || markIn == null || markOut == null) {
            null
        } else {
            SkipableTimeInterval(
                id = type.name,
                start = markIn,
                end = markOut,
            )
        }
    }
}
