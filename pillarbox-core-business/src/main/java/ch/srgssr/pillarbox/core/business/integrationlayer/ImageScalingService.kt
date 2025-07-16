/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import java.net.URLEncoder

/**
 * Service used to get a scaled image URL. This only works for SRG images.
 *
 * @param ilHost Base URL of the service.
 */
internal class ImageScalingService(
    private val ilHost: IlHost = IlHost.PROD
) {

    fun getScaledImageUrl(
        imageUrl: String,
    ): String {
        val encodedImageUrl = URLEncoder.encode(imageUrl, Charsets.UTF_8.name())

        return "${ilHost.baseHostUrl}/images/?imageUrl=$encodedImageUrl&format=webp&width=960"
    }
}
