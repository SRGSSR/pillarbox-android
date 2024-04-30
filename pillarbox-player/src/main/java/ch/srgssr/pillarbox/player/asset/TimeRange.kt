/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import kotlin.math.abs

/**
 * Time interval
 */
interface TimeRange {
    /**
     * The id.
     */
    val id: String

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
     * Check if the provided [position][positionMs] is in this [interval][TimeRange].
     *
     * @param positionMs The position, in milliseconds.
     * @return `true` if [positionMs] is between [start] (included) and [end] (excluded).
     */
    operator fun contains(positionMs: Long): Boolean {
        return positionMs in start..<end
    }
}
