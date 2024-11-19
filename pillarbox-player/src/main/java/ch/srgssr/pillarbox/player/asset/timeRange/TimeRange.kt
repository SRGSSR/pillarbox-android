/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import androidx.media3.common.C
import kotlin.math.abs

/**
 * Time range
 */
sealed interface TimeRange {
    /**
     * The start position, in milliseconds, in the player timeline.
     */
    val start: Long

    /**
     * The end position, in milliseconds, in the player timeline.
     */
    val end: Long

    /**
     * Duration, in milliseconds.
     */
    val duration: Long
        get() {
            return abs(end - start)
        }

    /**
     * Check if the provided [position][positionMs] is in this [TimeRange].
     *
     * @param positionMs The position, in milliseconds.
     * @return `true` if [positionMs] is between [start] (included) and [end] (excluded).
     */
    operator fun contains(positionMs: Long): Boolean {
        return positionMs in start..<end
    }
}

/**
 * @return the first not `null` [TimeRange] at [position].
 */
internal fun <T : TimeRange> List<T>.firstOrNullAtPosition(position: Long): T? {
    return if (position == C.TIME_UNSET) {
        null
    } else {
        firstOrNull { position in it }
    }
}
