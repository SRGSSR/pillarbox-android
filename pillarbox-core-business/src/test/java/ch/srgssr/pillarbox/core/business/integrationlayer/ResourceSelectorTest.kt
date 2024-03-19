/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
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
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNull(result)
    }

    @Test
    fun testOneHlsWithIncompatibles() {
        val type = Resource.Type.HLS
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testOneDashWithIncompatibles() {
        val type = Resource.Type.DASH
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testOneProgressiveWithIncompatibles() {
        val type = Resource.Type.PROGRESSIVE
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testHlsFirstWithIncompatibles() {
        val type = Resource.Type.HLS
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type),
                createResource(Resource.Type.DASH),
                createResource(Resource.Type.PROGRESSIVE)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testDashFirstWithIncompatibles() {
        val type = Resource.Type.DASH
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type),
                createResource(Resource.Type.HLS),
                createResource(Resource.Type.PROGRESSIVE)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(type), result)
    }

    @Test
    fun testProgressiveFirstWithIncompatibles() {
        val type = Resource.Type.PROGRESSIVE
        val chapter = createChapter(
            listOf(
                createResource(Resource.Type.M3UPLAYLIST),
                createResource(Resource.Type.HDS),
                createResource(Resource.Type.RTMP),
                createResource(Resource.Type.UNKNOWN),
                createResource(type),
                createResource(Resource.Type.HLS),
                createResource(Resource.Type.DASH)
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
        val type = Resource.Type.HLS
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
                createResource(Resource.Type.HLS),
                createResource(Resource.Type.DASH),
                createSupportedDrmResource()
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createResource(Resource.Type.HLS), result)
    }

    @Test
    fun testSupportedAndUnsupportedDrm() {
        val chapter = createChapter(
            listOf(
                createUnsupportedDrmResource(),
                createSupportedDrmResource(),
                createResource(Resource.Type.HLS),
                createResource(Resource.Type.DASH),
                createResource(Resource.Type.RTMP)
            )
        )
        val result = resourceSelector.selectResourceFromChapter(chapter)
        assertNotNull(result)
        assertEquals(createSupportedDrmResource(), result)
    }

    companion object {
        private const val DUMMY_IMAGE_URL = "https://image.png"

        fun createChapter(listResource: List<Resource>?): Chapter {
            return Chapter(urn = "urn", listResource = listResource, title = "title", imageUrl = DUMMY_IMAGE_URL)
        }

        fun createResource(type: Resource.Type): Resource {
            return Resource(url = "", type = type)
        }

        fun createUnsupportedDrmResource(): Resource {
            return Resource("", Resource.Type.HLS, drmList = listOf(Drm(Drm.Type.FAIRPLAY, "")))
        }

        fun createSupportedDrmResource(): Resource {
            return Resource(
                url = "",
                type = Resource.Type.DASH,
                drmList = listOf(
                    Drm(Drm.Type.WIDEVINE, "https://widevine.license.co"),
                    Drm(Drm.Type.PLAYREADY, "https://playready.license.co")
                )
            )
        }
    }
}
