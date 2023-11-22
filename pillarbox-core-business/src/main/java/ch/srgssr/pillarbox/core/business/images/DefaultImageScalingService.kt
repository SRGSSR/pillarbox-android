/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.images

import android.net.Uri
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageFormat
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageWidth
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import java.net.URL

/**
 * Default service for scaling images. This only works for SRG images.
 *
 * @param baseUrl Base URL of the service.
 */
class DefaultImageScalingService(
    private val baseUrl: URL = IlHost.DEFAULT
) : ImageScalingService {
    override fun getScaledImageUrl(
        imageUrl: String,
        width: ImageWidth,
        format: ImageFormat
    ): String {
        return Uri.parse(baseUrl.toString())
            .buildUpon()
            .appendEncodedPath("images/")
            .appendQueryParameter("imageUrl", imageUrl)
            .appendQueryParameter("format", format.format)
            .appendQueryParameter("width", width.width.toString())
            .build()
            .toString()
    }
}
