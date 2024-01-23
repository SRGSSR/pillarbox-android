/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui

import kotlin.time.Duration

/**
 * @return a formatter function for displaying the duration based on its length.
 */
fun Duration.getFormatter(): (Duration) -> String {
    return if (inWholeHours <= 0) DurationFormatter::formatMinutesSeconds else DurationFormatter::formatHourMinutesSeconds
}

/**
 * Duration formatter
 */
object DurationFormatter {
    private const val FORMAT_HOURS_MINUTES_SECONDS = "%02d:%02d:%02d"
    private const val FORMAT_MINUTES_SECONDS = "%02d:%02d"

    /**
     * @param duration The duration to format.
     * @return Format hour minutes seconds
     */
    fun formatHourMinutesSeconds(duration: Duration): String {
        return duration.toComponents { hours, minutes, seconds, _ ->
            FORMAT_HOURS_MINUTES_SECONDS.format(hours, minutes, seconds)
        }
    }

    /**
     * @param duration The duration to format.
     * @return Format minutes seconds
     */
    fun formatMinutesSeconds(duration: Duration): String {
        return duration.toComponents { _, minutes, seconds, _ ->
            FORMAT_MINUTES_SECONDS.format(minutes, seconds)
        }
    }
}
