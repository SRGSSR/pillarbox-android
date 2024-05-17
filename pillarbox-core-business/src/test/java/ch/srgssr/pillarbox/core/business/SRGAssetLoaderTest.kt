/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.BlockReason
import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.MediaComposition
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Quality
import ch.srg.dataProvider.integrationlayer.data.remote.Resource
import ch.srg.dataProvider.integrationlayer.data.remote.Segment
import ch.srg.dataProvider.integrationlayer.data.remote.StreamingMethod
import ch.srg.dataProvider.integrationlayer.data.remote.TimeInterval
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.source.SegmentAdapter
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter
import ch.srgssr.pillarbox.player.extension.credits
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class SRGAssetLoaderTest {

    private val mediaCompositionService = DummyMediaCompositionProvider()
    private lateinit var assetLoader: SRGAssetLoader

    @BeforeTest
    fun init() {
        val context: Context = ApplicationProvider.getApplicationContext()
        assetLoader = SRGAssetLoader(context, mediaCompositionService)
    }

    @Test(expected = IllegalStateException::class)
    fun testNoMediaId() = runTest {
        assetLoader.loadAsset(MediaItem.Builder().build())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidMediaId() = runTest {
        assetLoader.loadAsset(SRGMediaItemBuilder("urn:rts:show:radio:1234").build())
    }

    @Test(expected = ResourceNotFoundException::class)
    fun testNoResource() = runTest {
        assetLoader.loadAsset(SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_NO_RESOURCES).build())
    }

    @Test(expected = ResourceNotFoundException::class)
    fun testNoCompatibleResource() = runTest {
        assetLoader.loadAsset(SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_INCOMPATIBLE_RESOURCE).build())
    }

    @Test
    fun testCompatibleResource() = runTest {
        assetLoader.loadAsset(SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_HLS_RESOURCE).build())
    }

    @Test
    fun testMetadata() = runTest {
        val asset = assetLoader.loadAsset(SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_METADATA).build())
        val metadata = asset.mediaMetadata
        val expected =
            MediaMetadata.Builder()
                .setTitle("Title")
                .setSubtitle("Lead")
                .setDescription("Description")
                .setArtworkUri(metadata.artworkUri)
                .build()
        assertEquals(expected, metadata)
    }

    @Test
    fun testWithCustomMetadata() = runTest {
        val input = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .setSubtitle("CustomSubtitle")
            .setDescription("CustomDescription")
            .build()

        val asset = assetLoader.loadAsset(
            SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_METADATA)
                .setMediaMetadata(input)
                .build()
        )

        val metadata = asset.mediaMetadata
        val expected = input.buildUpon()
            .setArtworkUri(ImageScalingService().getScaledImageUrl(DummyMediaCompositionProvider.DUMMY_IMAGE_URL).toUri())
            .build()
        assertEquals(expected, metadata)
    }

    @Test
    fun testWithPartialCustomMetadata() = runTest {
        val input = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .build()
        val asset = assetLoader.loadAsset(
            SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_METADATA).setMediaMetadata(input).build()
        )
        val metadata = asset.mediaMetadata
        val expected = MediaMetadata.Builder()
            .setTitle("CustomTitle")
            .setSubtitle("Lead")
            .setDescription("Description")
            .setArtworkUri(ImageScalingService().getScaledImageUrl(DummyMediaCompositionProvider.DUMMY_IMAGE_URL).toUri())
            .build()
        assertEquals(expected, metadata)
    }

    @Test
    fun testCustomMetadataProvider() = runTest {
        assetLoader.mediaMetadataProvider = SRGAssetLoader.MediaMetadataProvider { mediaMetadataBuilder, _, _, _ ->
            mediaMetadataBuilder.setTitle("My custom title")
            mediaMetadataBuilder.setSubtitle("My custom subtitle")
        }
        val asset = assetLoader.loadAsset(SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_METADATA).build())
        val expected = MediaMetadata.Builder()
            .setTitle("My custom title")
            .setSubtitle("My custom subtitle")
            .build()
        assertEquals(expected, asset.mediaMetadata)
    }

    @Test(expected = BlockReasonException::class)
    fun testBlockReason() = runTest {
        assetLoader.loadAsset(
            SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_BLOCK_REASON).build()
        )
    }

    @Test
    fun testBlockedSegmentFillAssetBlockedIntervals() = runTest {
        val asset = assetLoader.loadAsset(
            SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_SEGMENT_BLOCK_REASON).build()
        )
        val expectedBlockTimeRanges = listOf(SegmentAdapter.getBlockedTimeRange(DummyMediaCompositionProvider.BLOCKED_SEGMENT))
        assertEquals(expectedBlockTimeRanges, asset.blockedTimeRanges)
    }

    @Test
    fun testTimeIntervals() = runTest {
        val asset = assetLoader.loadAsset(
            SRGMediaItemBuilder(DummyMediaCompositionProvider.URN_TIME_INTERVALS).build()
        )
        val expectedCredits = TimeIntervalAdapter.getCredits(
            listOf(DummyMediaCompositionProvider.TIME_INTERVAL_1, DummyMediaCompositionProvider.TIME_INTERVAL_2)
        )
        assertEquals(expectedCredits, asset.mediaMetadata.credits)
    }

    internal class DummyMediaCompositionProvider : MediaCompositionService {

        override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
            return when (val urn = uri.lastPathSegment) {
                URN_NO_RESOURCES -> Result.success(createMediaComposition(urn, null))
                URN_EMPTY_RESOURCES -> Result.success(createMediaComposition(urn, emptyList()))
                URN_HLS_RESOURCE -> Result.success(createMediaComposition(urn, listOf(createResource(StreamingMethod.HLS))))
                URN_INCOMPATIBLE_RESOURCE -> Result.success(
                    createMediaComposition(
                        urn = urn,
                        resourceList = listOf(createResource(StreamingMethod.UNKNOWN)),
                    )
                )

                URN_METADATA -> {
                    val chapter = createChapter(
                        urn = urn,
                        title = "Title",
                        lead = "Lead",
                        description = "Description",
                        resourceList = listOf(createResource(StreamingMethod.HLS)),
                        imageUrl = DUMMY_IMAGE_URL,
                        segmentList = listOf(SEGMENT_1, SEGMENT_2),
                        mediaType = MediaType.VIDEO,
                    )
                    Result.success(
                        MediaComposition(
                            chapterUrn = urn,
                            chapterList = listOf(chapter),
                        )
                    )
                }

                URN_BLOCK_REASON -> {
                    val mainChapter = createChapter(
                        urn = urn,
                        title = "Blocked media",
                        blockReason = BlockReason.UNKNOWN,
                        resourceList = listOf(createResource(StreamingMethod.HLS)),
                        imageUrl = DUMMY_IMAGE_URL,
                        segmentList = listOf(SEGMENT_1, SEGMENT_2),
                        mediaType = MediaType.VIDEO,
                    )

                    Result.success(
                        MediaComposition(
                            chapterUrn = urn,
                            chapterList = listOf(mainChapter),
                        )
                    )
                }

                URN_SEGMENT_BLOCK_REASON -> {
                    val mainChapter = createChapter(
                        urn = urn,
                        title = "Blocked segment media",
                        blockReason = null,
                        resourceList = listOf(createResource(StreamingMethod.HLS)),
                        imageUrl = DUMMY_IMAGE_URL,
                        segmentList = listOf(SEGMENT_1, BLOCKED_SEGMENT),
                        mediaType = MediaType.VIDEO,
                    )
                    Result.success(
                        MediaComposition(
                            chapterUrn = urn,
                            chapterList = listOf(mainChapter, CHAPTER_1, CHAPTER_2),
                        )
                    )
                }

                URN_TIME_INTERVALS -> {
                    val mainChapter = createChapter(
                        urn = urn,
                        title = "Time intervals",
                        resourceList = listOf(createResource(StreamingMethod.HLS)),
                        imageUrl = DUMMY_IMAGE_URL,
                        segmentList = null,
                        mediaType = MediaType.VIDEO,
                        timeIntervalList = listOf(TIME_INTERVAL_1, TIME_INTERVAL_2),
                    )
                    Result.success(
                        MediaComposition(
                            chapterUrn = urn,
                            chapterList = listOf(mainChapter),
                        )
                    )
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
            const val URN_SEGMENT_BLOCK_REASON = "urn:rts:video:segment_block_reason"
            const val URN_TIME_INTERVALS = "urn:rts:video:time_intervals"
            const val DUMMY_IMAGE_URL = "https://image.png"
            val SEGMENT_1 = createSegment(
                urn = "s1",
                title = "title",
                markIn = 0L,
                markOut = 1L,
            )
            val SEGMENT_2 = createSegment(
                urn = "s2",
                title = "title",
                markIn = 2L,
                markOut = 3L,
            )
            val BLOCKED_SEGMENT = createSegment(
                urn = "blocked",
                title = "Blocked segment",
                markIn = 4L,
                markOut = 5L,
                blockReason = BlockReason.UNKNOWN,
            )

            val TIME_INTERVAL_1 = TimeInterval(
                markIn = 10L,
                markOut = 20L,
                type = TimeInterval.Type.OPENING_CREDITS,
            )
            val TIME_INTERVAL_2 = TimeInterval(
                markIn = 40L,
                markOut = 100L,
                type = TimeInterval.Type.CLOSING_CREDITS,
            )

            val CHAPTER_1 = createChapter(
                urn = "urn:chapter1",
                title = "Blocked segment media",
                blockReason = null,
                resourceList = listOf(createResource(StreamingMethod.HLS)),
                imageUrl = DUMMY_IMAGE_URL,
                fullLengthUrn = "urn:full_length",
                fullLengthMarkIn = 0,
                fullLengthMarkOut = 10,
                mediaType = MediaType.VIDEO,
            )

            val CHAPTER_2 = createChapter(
                urn = "urn:chapter2",
                title = "Blocked segment media",
                blockReason = null,
                resourceList = listOf(createResource(StreamingMethod.HLS)),
                imageUrl = DUMMY_IMAGE_URL,
                fullLengthUrn = "urn:full_length",
                fullLengthMarkIn = 20,
                fullLengthMarkOut = 30,
                mediaType = MediaType.VIDEO,
            )

            fun createMediaComposition(
                urn: String,
                resourceList: List<Resource>?,
            ): MediaComposition {
                return MediaComposition(
                    chapterUrn = urn,
                    chapterList = listOf(
                        createChapter(
                            urn = urn,
                            title = urn,
                            resourceList = resourceList,
                            imageUrl = DUMMY_IMAGE_URL,
                            mediaType = MediaType.VIDEO,
                        ),
                    ),
                )
            }

            fun createResource(streamingMethod: StreamingMethod): Resource {
                return Resource(
                    url = "",
                    quality = Quality.HD,
                    streamingMethod = streamingMethod,
                )
            }
        }
    }

    private companion object {
        private fun createChapter(
            urn: String,
            title: String,
            lead: String? = null,
            description: String? = null,
            blockReason: BlockReason? = null,
            resourceList: List<Resource>? = null,
            imageUrl: String,
            fullLengthUrn: String? = null,
            fullLengthMarkIn: Long? = null,
            fullLengthMarkOut: Long? = null,
            segmentList: List<Segment>? = null,
            mediaType: MediaType,
            timeIntervalList: List<TimeInterval>? = null,
        ): Chapter {
            return Chapter(
                id = "id",
                mediaType = mediaType,
                vendor = Vendor.RTS,
                urn = urn,
                title = title,
                lead = lead,
                description = description,
                imageUrl = ImageUrl(imageUrl),
                blockReason = blockReason,
                type = Type.CLIP,
                date = Clock.System.now(),
                duration = 0L,
                fullLengthUrn = fullLengthUrn,
                fullLengthMarkIn = fullLengthMarkIn,
                fullLengthMarkOut = fullLengthMarkOut,
                segmentList = segmentList,
                resourceList = resourceList,
                timeIntervalList = timeIntervalList,
            )
        }

        private fun createSegment(
            urn: String,
            title: String,
            markIn: Long,
            markOut: Long,
            blockReason: BlockReason? = null,
        ): Segment {
            return Segment(
                id = "id",
                mediaType = MediaType.VIDEO,
                vendor = Vendor.RTS,
                urn = urn,
                title = title,
                markIn = markIn,
                markOut = markOut,
                type = Type.CLIP,
                date = Clock.System.now(),
                duration = 0L,
                displayable = true,
                playableAbroad = true,
                imageUrl = ImageUrl(""),
                blockReason = blockReason,
            )
        }
    }
}
