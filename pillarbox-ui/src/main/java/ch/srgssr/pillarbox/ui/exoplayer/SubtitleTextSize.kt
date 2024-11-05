/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.exoplayer

import android.view.View
import android.view.accessibility.CaptioningManager
import androidx.annotation.Dimension

/**
 * Represents the text size for subtitles.
 *
 * This interface provides options for defining the subtitle text size, allowing for either a fractional size relative to the [View]'s height or a
 * fixed size with a specific unit.
 *
 * It overrides the caption font scale set by the user in the [CaptioningManager].
 */
sealed interface SubtitleTextSize {
    /**
     * Represents a text size calculated as a fraction of the [View]'s height, optionally ignoring the [View]'s vertical padding.
     *
     * @property fractionOfHeight The fraction of the [View]'s height that should be used to calculate the text size, between 0 and 1.
     * @property ignorePadding Whether to subtract the [View]'s vertical padding from its height before calculating the text size.
     */
    data class Fractional(
        val fractionOfHeight: Float,
        val ignorePadding: Boolean = false
    ) : SubtitleTextSize

    /**
     * Represents a fixed size for caption text.
     *
     * @property unit The unit of the text size. This should be one of the [Dimension] constants.
     * @property size The size of the text in the specified unit.
     */
    data class Fixed(@Dimension val unit: Int, val size: Float) : SubtitleTextSize
}
