/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.exoplayer

import androidx.annotation.Dimension

/**
 * Subtitle text size
 *
 * It will override the caption font scale defined by the user in the CaptionManager.
 */
sealed interface SubtitleTextSize {
    /**
     * Sets the text size to be a fraction of the view's remaining height after its top and bottom padding have been subtracted or not.
     *
     * @property fractionOfHeight A fraction between 0 and 1.
     * @property ignorePadding ignore padding during height computations.
     */
    data class Fractional(
        val fractionOfHeight: Float,
        val ignorePadding: Boolean = false
    ) : SubtitleTextSize

    /**
     * Specify a fixed size caption text.
     *
     * @property unit Unit of the size [Dimension]
     * @property size The size of the text in the unit dimension.
     */
    data class Fixed(@Dimension val unit: Int, val size: Float) : SubtitleTextSize
}
