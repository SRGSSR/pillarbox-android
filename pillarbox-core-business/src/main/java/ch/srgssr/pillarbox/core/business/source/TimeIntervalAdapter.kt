/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.player.asset.ActionableTimeInterval

internal object TimeIntervalAdapter {
    fun getTimeIntervals(timeIntervals: List<TimeInterval>?): List<ActionableTimeInterval> {
        return timeIntervals
            .orEmpty()
            .mapNotNull { timeInterval ->
                if (timeInterval.type == null || timeInterval.markIn == null || timeInterval.markOut == null) {
                    null
                } else {
                    ActionableTimeInterval(
                        id = timeInterval.type.name,
                        start = timeInterval.markIn,
                        end = timeInterval.markOut,
                    )
                }
            }
    }
}
