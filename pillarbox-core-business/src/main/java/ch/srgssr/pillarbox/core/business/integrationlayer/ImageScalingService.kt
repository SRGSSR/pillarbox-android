/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import java.net.URL
import java.net.URLEncoder

/**
 * Service used to get a scaled image URL. This only works for SRG images.
 *
 * @param baseUrl Base URL of the service.
 */
internal class ImageScalingService(
    private val baseUrl: URL = IlHost.DEFAULT
) {

    fun getScaledImageUrl(
        imageUrl: String,
    ): String {
        val encodedImageUrl = URLEncoder.encode(imageUrl, Charsets.UTF_8.name())

        return "${baseUrl}images/?imageUrl=$encodedImageUrl&format=webp&width=480"
    }
}
