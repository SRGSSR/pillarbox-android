package ch.srgssr.pillarbox.core.business.images

import ch.srgssr.pillarbox.core.business.images.ImageScaleService.ImageFormat
import ch.srgssr.pillarbox.core.business.images.ImageScaleService.ImageWidth
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class DefaultImageScaleServiceTest(
    private val imageUrl: String,
    private val width: ImageWidth,
    private val format: ImageFormat
) {
    private lateinit var imageScaleService: ImageScaleService

    @Before
    fun before() {
        imageScaleService = DefaultImageScaleService(baseUrl = IlHost.PROD)
    }

    @Test
    fun getScaledImageUrl() {
        val scaledImageUrl = imageScaleService.getScaledImageUrl(imageUrl, width, format)

        assertEquals("https://il.srgssr.ch/images/?imageUrl=$imageUrl&format=${format.format}&width=${width.width}", scaledImageUrl)
    }

    companion object {
        @JvmStatic
        @Parameters
        fun parameters(): Iterable<Any> {
            return listOf(
                arrayOf("https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9", ImageWidth.W240, ImageFormat.JPG),
                arrayOf("https://www.rts.ch/2021/08/05/18/12/12396566.image/2x3", ImageWidth.W320, ImageFormat.PNG),
                arrayOf("https://www.rts.ch/2022/10/06/17/32/13444418.image/4x5", ImageWidth.W480, ImageFormat.WEBP)
            )
        }
    }
}
