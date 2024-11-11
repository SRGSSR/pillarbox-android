/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.Player

/**
 * Represents the scaling mode for the [Player] content within its parent container.
 */
enum class ScaleMode {
    /**
     * Resizes the [Player][androidx.media3.common.Player] content to fit within the parent while maintaining its aspect ratio. This ensures the
     * entire content is visible, but may result in black bars (letterboxing or pillarboxing) on the sides or top/bottom if the aspect ratios of the
     * media and container do not match.
     */
    Fit,

    /**
     * Stretches the [Player][androidx.media3.common.Player] to fill its parent, ignoring the defined aspect ratio. This may cause the content to be
     * distorted horizontally or vertically to match the parent's dimensions.
     */
    Fill,

    /**
     * Trims the [Player][androidx.media3.common.Player] to fill its parent while maintaining its aspect ratio. Any content that extends beyond the
     * bounds of the container will be clipped.
     */
    Crop,
}
