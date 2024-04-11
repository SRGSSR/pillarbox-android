/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import kotlin.math.abs

/**
 * Time interval
 *
 * @constructor Create empty Time interval
 */
interface TimeInterval {
    /**
     * Id
     */
    val id: String

    /**
     * Start
     */
    val start: Long

    /**
     * End
     */
    val end: Long

    /**
     * Duration
     */
    val duration: Long
        get() {
            return abs(end - start)
        }

    /**
     * Contains
     *
     * @param position
     * @return
     */
    operator fun contains(position: Long): Boolean {
        return position in start..<end
    }
}
