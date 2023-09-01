/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business


import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class MediaCompositionMediaItemSourceTest {

    private val mediaItemSource = MediaCompositionMediaItemSource(DummyMediaCompositionProvider())

    @Test(expected = IllegalArgumentException::class)
    fun testNoMediaId() = runBlocking {
        mediaItemSource.loadMediaItem(MediaItem.Builder().build())
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidMediaId() = runBlocking {
        mediaItemSource.loadMediaItem(MediaItem.Builder().setMediaId("urn:rts:show:radio:1234").build())
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUrnAsUri() = runBlocking {
        val urn = "urn:rts:video:1234"
        mediaItemSource.loadMediaItem(MediaItem.Builder().setMediaId(urn).setUri(urn).build())
        Unit
    }

    @Test(expected = ResourceNotFoundException::class)
    fun testNoResource() = runBlocking {
        mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_NO_RESOURCES))
        Unit
    }

    @Test(expected = ResourceNotFoundException::class)
    fun testNoCompatibleResource() = runBlocking {
        mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_INCOMPATIBLE_RESOURCE))
        Unit
    }

    @Test
    fun testCompatibleResource() = runBlocking {
        val mediaItem = mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_HLS_RESOURCE))
        Assert.assertNotNull(mediaItem)
    }

    @Test
    fun testMetadata() = runBlocking {
        val mediaItem = mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_METADATA))
        Assert.assertNotNull(mediaItem)
        val metadata = mediaItem.mediaMetadata
        val expected = MediaMetadata.Builder()
            .setTitle("Title")
            .setSubtitle("Lead")
            .setDescription("Description")
            .build()
        Assert.assertEquals(expected, metadata)
    }

    @Test
    fun testWithCustomMetadata() = runBlocking {
        val input = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .setSubtitle("CustomSubtitle")
            .setDescription("CustomDescription")
            .build()
        val mediaItem = mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_METADATA, input))
        Assert.assertNotNull(mediaItem)
        val metadata = mediaItem.mediaMetadata
        val expected = input.buildUpon().build()
        Assert.assertEquals(expected, metadata)
    }

    @Test
    fun testWithPartialCustomMetadata() = runBlocking {
        val input = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .build()
        val mediaItem = mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_METADATA, input))
        Assert.assertNotNull(mediaItem)
        val metadata = mediaItem.mediaMetadata
        val expected = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .setSubtitle("Lead")
            .setDescription("Description")
            .build()
        Assert.assertEquals(expected, metadata)
    }

    @Test(expected = BlockReasonException::class)
    fun testBlockReason() = runBlocking {
        val input = MediaMetadata.Builder().build()
        mediaItemSource.loadMediaItem(createMediaItem(DummyMediaCompositionProvider.URN_BLOCK_REASON, input))
        Assert.assertTrue(false)
    }

    internal class DummyMediaCompositionProvider : MediaCompositionDataSource {

        override suspend fun getMediaCompositionByUrn(urn: String): Result<MediaComposition> {
            return when (urn) {
                URN_NO_RESOURCES -> Result.success(createMediaComposition(urn, null))
                URN_EMPTY_RESOURCES -> Result.success(createMediaComposition(urn, emptyList()))
                URN_HLS_RESOURCE -> Result.success(createMediaComposition(urn, listOf(createResource(Resource.Type.HLS))))
                URN_INCOMPATIBLE_RESOURCE -> Result.success(
                    createMediaComposition(
                        urn, listOf(
                            createResource(Resource.Type.UNKNOWN),
                        )
                    )
                )
                URN_METADATA -> {
                    val chapter = Chapter(
                        urn, title = "Title", lead = "Lead", description = "Description", listResource = listOf(
                            createResource(
                                Resource.Type
                                    .HLS
                            )
                        ),
                        imageUrl = DUMMY_IMAGE_URL
                    )
                    Result.success(MediaComposition(chapterUrn = urn, listChapter = listOf(chapter)))
                }
                URN_BLOCK_REASON -> {
                    val chapter = Chapter(
                        urn, title = "Blocked media", blockReason = "A block reason",
                        listResource = listOf(createResource(Resource.Type.HLS)),
                        imageUrl = DUMMY_IMAGE_URL
                    )
                    Result.success(MediaComposition(chapterUrn = urn, listChapter = listOf(chapter)))
                }
                else -> Result.failure(IllegalArgumentException("No resource found"))
            }
        }

        companion object {
            const val URN_NO_RESOURCES = "urn:rts:video:no_resources"
            const val URN_EMPTY_RESOURCES = "urn:rts:video:empty"
            const val URN_HLS_RESOURCE = "urn:rts:video:resource_hls"
            const val URN_METADATA = "urn:rts:video:resource_metadata"
            const val URN_INCOMPATIBLE_RESOURCE = "urn:rts:video:resource_incompatible"
            const val URN_BLOCK_REASON = "urn:rts:video:block_reason"
            const val DUMMY_IMAGE_URL ="https://image.png"

            fun createMediaComposition(urn: String, listResource: List<Resource>?): MediaComposition {
                return MediaComposition(urn, listOf(Chapter(urn = urn, title = urn, listResource = listResource, imageUrl = DUMMY_IMAGE_URL)))
            }

            fun createResource(type: Resource.Type): Resource {
                return Resource(url = "", type = type)
            }
        }
    }

    companion object {
        private fun createMediaItem(urn: String): MediaItem {
            return MediaItem.Builder().setMediaId(urn).build()
        }

        private fun createMediaItem(urn: String, metadata: MediaMetadata): MediaItem {
            return MediaItem.Builder()
                .setMediaMetadata(metadata)
                .setMediaId(urn)
                .build()
        }
    }
}
