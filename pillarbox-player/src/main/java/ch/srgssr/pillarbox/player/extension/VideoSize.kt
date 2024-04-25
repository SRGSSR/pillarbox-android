/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.util.Rational
import androidx.media3.common.VideoSize

/**
 * Compute the aspect ratio, return `null` if the aspect ratio can't be computed.
 */
fun VideoSize.computeAspectRatioOrNull(): Float? {
    return if (height == 0 || width == 0) null else width * this.pixelWidthHeightRatio / height
}

/**
 * Convert VideoSize to Rational that is useful for picture in picture
 *
 * @return a [Rational]
 */
fun VideoSize.toRational(): Rational {
    return if (this == VideoSize.UNKNOWN) {
        RATIONAL_ONE
    } else {
        Rational(width, height)
    }
}

/**
 * Rational One with a Rationale set to 1/1.
 */
val RATIONAL_ONE = Rational(1, 1)
