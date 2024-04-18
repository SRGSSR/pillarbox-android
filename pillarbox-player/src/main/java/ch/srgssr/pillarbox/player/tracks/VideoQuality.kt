/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracks

import androidx.annotation.Px
import androidx.media3.common.Format

/**
 * Represent a video quality.
 *
 * @param format The [Format] containing information about this video quality.
 * @property isSelected `true` if this video quality is selected, `false` otherwise.
 */
data class VideoQuality(
    private val format: Format,
    val isSelected: Boolean,
) {
    /**
     * The height of this video quality, in pixels.
     */
    @get:Px
    val height: Int
        get() = format.height

    /**
     * The width of this video quality, in pixels.
     */
    @get:Px
    val width: Int
        get() = format.width
}
