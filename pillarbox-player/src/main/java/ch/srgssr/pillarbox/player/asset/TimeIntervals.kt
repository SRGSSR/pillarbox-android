/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata

sealed interface TimeInterval {
    val id: String
    val start: Long
    val end: Long

    operator fun contains(position: Long): Boolean {
        return position in start..<end
    }
}

data class BlockedInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: Reason,
) : TimeInterval {
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

data class ChapterInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val mediaMetadata: MediaMetadata,
) : TimeInterval

data class Event(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val payload: Any? = null,
) : TimeInterval
