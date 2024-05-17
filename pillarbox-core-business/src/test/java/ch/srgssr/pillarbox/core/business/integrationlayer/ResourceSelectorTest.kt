/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Quality
import ch.srg.dataProvider.integrationlayer.data.remote.Resource
import ch.srg.dataProvider.integrationlayer.data.remote.Resource.Drm
import ch.srg.dataProvider.integrationlayer.data.remote.StreamingMethod
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ResourceSelectorTest {

    private val resourceSelector = ResourceSelector()

    @Test
    fun testNull() {
        val chapter = createChapter(null)
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNull(result)
    }

    @Test
    fun testEmptyList() {
        val chapter = createChapter(emptyList())
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNull(result)
    }

    @Test
    fun testOnlyNotCompatibleResources() {
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNull(result)
    }

    @Test
    fun testOneHlsWithIncompatibles() {
        val type = StreamingMethod.HLS
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testOneDashWithIncompatibles() {
        val type = StreamingMethod.DASH
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testOneProgressiveWithIncompatibles() {
        val type = StreamingMethod.PROGRESSIVE
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testHlsFirstWithIncompatibles() {
        val type = StreamingMethod.HLS
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type),
                createResource(StreamingMethod.DASH),
                createResource(StreamingMethod.PROGRESSIVE),
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testDashFirstWithIncompatibles() {
        val type = StreamingMethod.DASH
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type),
                createResource(StreamingMethod.HLS),
                createResource(StreamingMethod.PROGRESSIVE),
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testProgressiveFirstWithIncompatibles() {
        val type = StreamingMethod.PROGRESSIVE
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.M3UPLAYLIST),
                createResource(StreamingMethod.HDS),
                createResource(StreamingMethod.RTMP),
                createResource(StreamingMethod.UNKNOWN),
                createResource(type),
                createResource(StreamingMethod.HLS),
                createResource(StreamingMethod.DASH),
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testUnsupportedDrmOnly() {
        val chapter = createChapter(listOf(createUnsupportedDrmResource()))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNull(result)
    }

    @Test
    fun testUnsupportedDrm() {
        val type = StreamingMethod.HLS
        val chapter = createChapter(
            listOf(
                createUnsupportedDrmResource(),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testSupportedDrmOnly() {
        val chapter = createChapter(
            listOf(
                createSupportedDrmResource()
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createSupportedDrmResource(), result)
    }

    @Test
    fun testSupportedDrm() {
        val chapter = createChapter(
            listOf(
                createResource(StreamingMethod.HLS),
                createResource(StreamingMethod.DASH),
                createSupportedDrmResource()
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(StreamingMethod.HLS), result)
    }

    @Test
    fun testSupportedAndUnsupportedDrm() {
        val chapter = createChapter(
            listOf(
                createUnsupportedDrmResource(),
                createSupportedDrmResource(),
                createResource(StreamingMethod.HLS),
                createResource(StreamingMethod.DASH),
                createResource(StreamingMethod.RTMP),
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createSupportedDrmResource(), result)
    }

    companion object {
        private const val DUMMY_IMAGE_URL = "https://image.png"

        fun createChapter(resourceList: List<Resource>?): Chapter {
            return Chapter(
                id = "id",
                mediaType = MediaType.AUDIO,
                vendor = Vendor.RTS,
                urn = "urn",
                title = "title",
                imageUrl = ImageUrl(DUMMY_IMAGE_URL),
                type = Type.CLIP,
                date = Clock.System.now(),
                duration = 0L,
                resourceList = resourceList,
            )
        }

        fun createResource(streamingMethod: StreamingMethod): Resource {
            return Resource(
                url = "",
                quality = Quality.HD,
                streamingMethod = streamingMethod,
            )
        }

        fun createUnsupportedDrmResource(): Resource {
            return Resource(
                url = "",
                drmList = listOf(
                    Drm(
                        type = Drm.Type.FAIRPLAY,
                        licenseUrl = "",
                        certificateUrl = null,
                    ),
                ),
                quality = Quality.HD,
                streamingMethod = StreamingMethod.HLS,
            )
        }

        fun createSupportedDrmResource(): Resource {
            return Resource(
                url = "",
                drmList = listOf(
                    Drm(
                        type = Drm.Type.WIDEVINE,
                        licenseUrl = "https://widevine.license.co",
                        certificateUrl = null,
                    ),
                    Drm(
                        type = Drm.Type.PLAYREADY,
                        licenseUrl = "https://playready.license.co",
                        certificateUrl = null,
                    ),
                ),
                quality = Quality.HD,
                streamingMethod = StreamingMethod.DASH,
            )
        }
    }
}
