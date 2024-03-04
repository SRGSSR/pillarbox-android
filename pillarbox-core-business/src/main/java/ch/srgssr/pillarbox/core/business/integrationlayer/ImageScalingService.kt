/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import java.net.URL

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
        return URLBuilder(baseUrl.toString())
            .appendEncodedPathSegments("images/")
            .apply {
                parameters.append("imageUrl", imageUrl)
                parameters.append("format", "webp")
                parameters.append("width", "480")
            }
            .build()
            .toString()
    }
}
