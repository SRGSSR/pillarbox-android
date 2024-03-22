/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata

/**
 * A time interval inside a media.
 */
sealed interface TimeInterval {
    /**
     * The id of the interval.
     */
    val id: String

    /**
     * The start position of the interval, in milliseconds.
     */
    val start: Long

    /**
     * The end position of the interval, in milliseconds.
     */
    val end: Long

    /**
     * Check if the provided [position], in milliseconds, is inside the interval.
     *
     * @param position The position to check, in milliseconds.
     *
     * @return `true` if the [position] is in the interval, `false` otherwise
     */
    operator fun contains(position: Long): Boolean {
        return position in start..<end
    }
}

/**
 * A blocked interval in the associated media.
 *
 * @property id The id of the blocked interval.
 * @property start The start position of the blocked interval, in milliseconds.
 * @property end The end position of the blocked interval, in milliseconds.
 * @property reason The reason why this interval is blocked.
 */
data class BlockedInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: Reason,
) : TimeInterval {
    /**
     * Block reason
     */
    enum class Reason {
        GEOBLOCK,
        LEGAL,
        COMMERCIAL,
        AGERATING18,
        AGERATING12,
        STARTDATE,
        ENDDATE,
        UNKNOWN,
    }
}

/**
 * A chapter in the associated media.
 *
 * @property id The id of the chapter.
 * @property start The start position of the chapter, in milliseconds.
 * @property end The end position of the chapter, in milliseconds.
 * @property mediaMetadata The metadata of the chapter.
 */
data class ChapterInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val mediaMetadata: MediaMetadata,
) : TimeInterval

/**
 * A generic section in the associated media.
 *
 * @property id The id of the event.
 * @property start The start position of the event, in milliseconds.
 * @property end The end position of the event, in milliseconds.
 * @property payload Custom data associated with this event.
 */
data class Event(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val payload: Any? = null,
) : TimeInterval
