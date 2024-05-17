/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Chapter
import ch.srg.dataProvider.integrationlayer.data.remote.MediaComposition
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.source.ChapterAdapter
import kotlinx.datetime.Clock
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter as TimeRangeChapter

@RunWith(AndroidJUnit4::class)
class ChapterAdapterTest {

    @Test(expected = IllegalArgumentException::class)
    fun `main chapter to asset chapter throw exception`() {
        val chapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
        )
        ChapterAdapter.toChapter(chapter)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `main chapter with fullLengthMarkIn only to asset chapter throw exception`() {
        val chapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            fullLengthMarkIn = 10,
            mediaType = MediaType.VIDEO,
        )
        ChapterAdapter.toChapter(chapter)
    }

    @Test
    fun `chapter to asset chapter`() {
        val chapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            fullLengthMarkIn = 10,
            fullLengthMarkOut = 100,
            mediaType = MediaType.VIDEO,
        )
        val expected = TimeRangeChapter(
            id = "urn",
            start = 10,
            end = 100,
            mediaMetadata = MediaMetadata.Builder()
                .setTitle("title")
                .setDescription("lead")
                .setArtworkUri(Uri.parse(ImageScalingService().getScaledImageUrl("https://www.rts.ch/image.png")))
                .build()
        )
        assertEquals(expected, ChapterAdapter.toChapter(chapter))
    }

    @Test
    fun `only main chapter return empty asset chapter list`() {
        val mainChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val mediaComposition = MediaComposition(
            chapterUrn = mainChapter.urn,
            chapterList = listOf(mainChapter),
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main chapter with other chapter`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2),
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main chapter with chapters return asset chapter list without main chapter`() {
        val mainChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val chapter1 = mainChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = mainChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = mainChapter.urn,
            chapterList = listOf(mainChapter, chapter1, chapter2),
        )
        val expected = listOf(ChapterAdapter.toChapter(chapter1), ChapterAdapter.toChapter(chapter2))
        assertEquals(expected, ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `chapter with chapters return empty asset chapter list`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:chapter1",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2),
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `chapter audio with chapters return empty asset chapter list`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2),
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main audio chapter with chapters return empty asset chapter list`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2),
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main video chapter with mixed audio and video chapters return only video chapters`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val chapter3 = fullLengthChapter.copy(
            urn = "urn:chapter3", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn", mediaType = MediaType.AUDIO
        )
        val mediaComposition = MediaComposition(
            chapterUrn = "urn",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2, chapter3),
        )
        assertEquals(
            listOf(chapter1, chapter2).map {
                ChapterAdapter.toChapter(it)
            },
            ChapterAdapter.getChapters(mediaComposition)
        )
    }

    @Test
    fun `main chapter with chapter not related to main chapter are removed`() {
        val fullLengthChapter = createChapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "other urn")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn",
            chapterList = listOf(fullLengthChapter, chapter1, chapter2),
        )
        assertEquals(listOf(chapter1).map { ChapterAdapter.toChapter(it) }, ChapterAdapter.getChapters(mediaComposition))
    }

    private companion object {
        private fun createChapter(
            urn: String,
            title: String,
            lead: String? = null,
            description: String? = null,
            imageUrl: String,
            fullLengthMarkIn: Long? = null,
            fullLengthMarkOut: Long? = null,
            mediaType: MediaType,
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
                type = Type.CLIP,
                date = Clock.System.now(),
                duration = 0L,
                fullLengthMarkIn = fullLengthMarkIn,
                fullLengthMarkOut = fullLengthMarkOut,
            )
        }
    }
}
