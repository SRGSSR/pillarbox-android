/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

/**
 * Scale mode
 */
enum class ScaleMode {
    /**
     * Fit player content to the parent container and keep aspect ratio.
     */
    Fit,

    /**
     * Fill player content to the parent container.
     */
    Fill,

    /**
     * Crop player content inside the parent container and keep aspect ratio. Content outside the parent container will be clipped.
     */
    Crop,

    /**
     * Zoom, like [Crop] but doesn't clip content to the parent container. Useful for fullscreen mode.
     */
    Zoom
}
