/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata
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

/**
 * Blocked section
 *
 * @property id
 * @property start
 * @property end
 * @property reason
 * @constructor Create empty Blocked section
 */
data class BlockedSection(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: String
) : TimeInterval

/**
 * Chapter
 *
 * @property id
 * @property start
 * @property end
 * @property mediaMetadata
 * @constructor Create empty Chapter
 */
data class Chapter(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val mediaMetadata: MediaMetadata
) :
    TimeInterval
