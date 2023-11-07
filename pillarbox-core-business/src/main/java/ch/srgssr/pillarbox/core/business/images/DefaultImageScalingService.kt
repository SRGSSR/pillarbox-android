/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.images

import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageFormat
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageWidth
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import java.net.URL

/**
 * Default service for scaling images. This only works for SRG images.
 *
 * @property baseUrl Base URL of the service.
 */
class DefaultImageScalingService(
    private val baseUrl: URL = IlHost.DEFAULT
) : ImageScalingService {
    override fun getScaledImageUrl(
        imageUrl: String,
        width: ImageWidth,
        format: ImageFormat
    ): String {
        return "${baseUrl}images/?imageUrl=$imageUrl&format=${format.format}&width=${width.width}"
    }
}
