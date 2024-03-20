/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata

interface TimeInterval {
    val id: String
    val start: Long
    val end: Long
}

data class BlockedInterval(override val id: String, override val start: Long, override val end: Long, val reason: String) : TimeInterval

data class ChapterInterval(override val id: String, override val start: Long, override val end: Long, val mediaMetadata: MediaMetadata) :
    TimeInterval
