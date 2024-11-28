/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import androidx.media3.common.C
import kotlin.math.abs

/**
 * Represents a time range within a media playback timeline.
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
     * The duration of the time range, in milliseconds.
     */
    val duration: Long
        get() {
            return abs(end - start)
        }

    /**
     * Checks if the provided [position][positionMs], in milliseconds, is within this [TimeRange].
     *
     * @param positionMs The position to check, in milliseconds.
     * @return Whether the [positionMs] is between [start] (inclusive) and [end] (exclusive).
     */
    operator fun contains(positionMs: Long): Boolean {
        return positionMs in start..<end
    }
}

/**
 * Returns the first [TimeRange] element in the list that contains the specified [position].
 *
 * @param T The type of [TimeRange].
 * @param position The position to search for within this list of [TimeRange]s.
 * @return The first [TimeRange] element containing the [position], or `null` if no such element is found.
 */
fun <T : TimeRange> List<T>.firstOrNullAtPosition(position: Long): T? {
    return if (position == C.TIME_UNSET) {
        null
    } else {
        firstOrNull { position in it }
    }
}
