/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.util.Rational
import androidx.media3.common.VideoSize

/**
 * Computes the aspect ratio of the video.
 *
 * @return The aspect ratio as a float, or `null` if it cannot be computed.
 */
fun VideoSize.computeAspectRatioOrNull(): Float? {
    if (height == 0 || width == 0) return null
    val par = pixelWidthHeightRatio
    var w = width.toFloat()
    var h = height.toFloat()
    if (par < 1.0) {
        w *= par
    } else if (par > 1.0) {
        h /= par
    }
    return w / h
}

/**
 * Converts this [VideoSize] to a [Rational] representation, which is particularly useful for scenarios like picture-in-picture.
 *
 * @return A [Rational] representing the aspect ratio of the [VideoSize].
 */
fun VideoSize.toRational(): Rational {
    return if (this == VideoSize.UNKNOWN) {
        RATIONAL_ONE
    } else {
        Rational(width, height)
    }
}

/**
 * Represents the rational number one (1/1).
 */
val RATIONAL_ONE = Rational(1, 1)
