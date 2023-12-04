/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLEncoder

class ImageScalingServiceTest {
    @Test
    fun getScaledImageUrlProd() {
        val baseUrl = IlHost.PROD
        val imageUrl = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9"


        val imageScalingService = ImageScalingService(baseUrl)
        val scaledImageUrl = imageScalingService.getScaledImageUrl(imageUrl)
        val encodedImageUrl = URLEncoder.encode(imageUrl, Charsets.UTF_8)

        assertEquals("${baseUrl}images/?imageUrl=$encodedImageUrl&format=webp&width=480", scaledImageUrl)
    }

    @Test
    fun getScaledImageUrlTest() {
        val baseUrl = IlHost.TEST
        val imageUrl = "https://www.rts.ch/2021/08/05/18/12/12396566.image/2x3"

        val imageScalingService = ImageScalingService(baseUrl)
        val scaledImageUrl = imageScalingService.getScaledImageUrl(imageUrl)
        val encodedImageUrl = URLEncoder.encode(imageUrl, Charsets.UTF_8)

        assertEquals("${baseUrl}images/?imageUrl=$encodedImageUrl&format=webp&width=480", scaledImageUrl)
    }

    @Test
    fun getScaledImageUrlStage() {
        val baseUrl = IlHost.STAGE
        val imageUrl = "https://www.rts.ch/2022/10/06/17/32/13444418.image/4x5"

        val imageScalingService = ImageScalingService(baseUrl)
        val scaledImageUrl = imageScalingService.getScaledImageUrl(imageUrl)
        val encodedImageUrl = URLEncoder.encode(imageUrl, Charsets.UTF_8)

        assertEquals("${baseUrl}images/?imageUrl=$encodedImageUrl&format=webp&width=480", scaledImageUrl)
    }
}
