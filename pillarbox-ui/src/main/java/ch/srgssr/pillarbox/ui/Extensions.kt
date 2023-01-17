/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.VideoSize

/**
 * Compute aspect ratio, return [unknownAspectRatioValue] if aspect ratio can't be computed.
 *
 * @param unknownAspectRatioValue
 */
fun VideoSize.computeAspectRatio(unknownAspectRatioValue: Float): Float {
    return if (height == 0 || width == 0) unknownAspectRatioValue else width * this.pixelWidthHeightRatio / height
}
