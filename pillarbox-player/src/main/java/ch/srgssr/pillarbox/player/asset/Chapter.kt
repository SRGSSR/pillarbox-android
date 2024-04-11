/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata

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
