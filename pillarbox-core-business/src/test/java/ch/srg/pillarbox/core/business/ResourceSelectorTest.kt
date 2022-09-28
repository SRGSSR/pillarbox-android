/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business

import ch.srg.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srg.pillarbox.core.business.integrationlayer.data.Resource
import org.junit.Assert
import org.junit.Test

class ResourceSelectorTest {

    private val resourceSelector = MediaCompositionMediaItemSource.ResourceSelector()

    @Test
    fun testNull() {
        val chapter = createChapter(null)
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNull(result)
    }

    @Test
    fun testEmptyList() {
        val chapter = createChapter(emptyList())
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNull(result)
    }

    @Test
    fun testOnlyNotCompatibleResources() {
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNull(result)
    }

    @Test
    fun testOneHlsWithIncompatibles() {
        val type = Resource.Type.HLS
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    @Test
    fun testOneDashWithIncompatibles() {
        val type = Resource.Type.DASH
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    @Test
    fun testOneProgressiveWithIncompatibles() {
        val type = Resource.Type.PROGRESSIVE
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    @Test
    fun testHlsFirstWithIncompatibles() {
        val type = Resource.Type.HLS
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type),
            createResource(Resource.Type.DASH),
            createResource(Resource.Type.PROGRESSIVE)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    @Test
    fun testDashFirstWithIncompatibles() {
        val type = Resource.Type.DASH
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type),
            createResource(Resource.Type.HLS),
            createResource(Resource.Type.PROGRESSIVE)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    @Test
    fun testProgressiveFirstWithIncompatibles() {
        val type = Resource.Type.PROGRESSIVE
        val chapter = createChapter(listOf(
            createResource(Resource.Type.M3UPLAYLIST),
            createResource(Resource.Type.HDS),
            createResource(Resource.Type.RTMP),
            createResource(Resource.Type.UNKNOWN),
            createResource(type),
            createResource(Resource.Type.HLS),
            createResource(Resource.Type.DASH)))
        val result = resourceSelector.selectResourceFromChapter(chapter)
        Assert.assertNotNull(result)
        Assert.assertEquals(createResource(type), result)
    }

    companion object {
        fun createChapter(listResource: List<Resource>?): Chapter {
            return Chapter(urn = "urn", listResource = listResource, title = "title")
        }

        fun createResource(type: Resource.Type): Resource {
            return Resource(url = "", type = type)
        }
    }
}
