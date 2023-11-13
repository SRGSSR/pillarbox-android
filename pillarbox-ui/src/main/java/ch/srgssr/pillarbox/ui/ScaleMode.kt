/*
 * Copyright (c) SRG SSR. All rights reserved.
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
     * Crop player content inside the parent container and keep aspect ratio.
     */
    Crop,
}
