/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.images

import android.net.Uri
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageFormat
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageWidth
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.net.URL

@RunWith(Parameterized::class)
class DefaultImageScalingServiceTest(
    private val baseUrl: URL,
    private val imageUrl: String,
    private val width: ImageWidth,
    private val format: ImageFormat
) {
    private lateinit var imageScalingService: ImageScalingService

    @Before
    fun before() {
        imageScalingService = DefaultImageScalingService(baseUrl)
    }

    @Test
    fun getScaledImageUrl() {
        val scaledImageUrl = imageScalingService.getScaledImageUrl(imageUrl, width, format)
        val encodedImageUrl = Uri.encode(imageUrl)

        assertEquals("${baseUrl}images/?imageUrl=$encodedImageUrl&format=${format.format}&width=${width.width}", scaledImageUrl)
    }

    companion object {
        @JvmStatic
        @Parameters
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf(IlHost.PROD, "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9", ImageWidth.W240, ImageFormat.JPG),
                arrayOf(IlHost.TEST, "https://www.rts.ch/2021/08/05/18/12/12396566.image/2x3", ImageWidth.W320, ImageFormat.PNG),
                arrayOf(IlHost.STAGE, "https://www.rts.ch/2022/10/06/17/32/13444418.image/4x5", ImageWidth.W480, ImageFormat.WEBP)
            )
        }
    }
}
