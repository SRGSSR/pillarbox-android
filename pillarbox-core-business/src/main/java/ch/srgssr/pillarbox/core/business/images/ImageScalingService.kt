/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.images

import androidx.annotation.Px

/**
 * Service used to get a scaled image URL.
 */
interface ImageScalingService {
    /**
     * The supported widths.
     *
     * @property width The width in pixels.
     */
    enum class ImageWidth(@Px val width: Int) {
        W240(width = 240),
        W320(width = 320),
        W480(width = 480),
        W960(width = 960),
        W1920(width = 1920)
    }

    /**
     * The supported image formats.
     *
     * @property format The format name.
     */
    enum class ImageFormat(val format: String) {
        JPG(format = "jpg"),
        PNG(format = "png"),
        WEBP(format = "webp")
    }

    /**
     * Get the URL of the scaled image, at the specified size and format, keeping the aspect ratio.
     *
     * @param imageUrl The original image URL.
     * @param width The desired width of the image.
     * @param format The desired format of the image.
     *
     * @return The URL of the scaled image.
     */
    fun getScaledImageUrl(
        imageUrl: String,
        width: ImageWidth,
        format: ImageFormat
    ): String
}
