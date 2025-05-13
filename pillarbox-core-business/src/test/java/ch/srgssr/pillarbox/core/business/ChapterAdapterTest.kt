/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Type
import ch.srgssr.pillarbox.core.business.source.ChapterAdapter
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter as TimeRangeChapter

@RunWith(AndroidJUnit4::class)
class ChapterAdapterTest {

    @Test(expected = IllegalArgumentException::class)
    fun `main chapter to asset chapter throw exception`() {
        val chapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
            type = Type.EPISODE,
        )
        ChapterAdapter.toChapter(chapter)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `main chapter with fullLengthMarkIn only to asset chapter throw exception`() {
        val chapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            fullLengthMarkIn = 10,
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        ChapterAdapter.toChapter(chapter)
    }

    @Test
    fun `chapter to asset chapter`() {
        val chapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            fullLengthMarkIn = 10,
            fullLengthMarkOut = 100,
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
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
        val mainChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val mediaComposition = MediaComposition(chapterUrn = mainChapter.urn, listChapter = listOf(mainChapter))
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main chapter with other chapter`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main chapter with chapters return asset chapter list without main chapter`() {
        val mainChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val chapter1 = mainChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = mainChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(chapterUrn = mainChapter.urn, listChapter = listOf(mainChapter, chapter1, chapter2))
        val expected = listOf(ChapterAdapter.toChapter(chapter1), ChapterAdapter.toChapter(chapter2))
        assertEquals(expected, ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `chapter with chapters return empty asset chapter list`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn:chapter1", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `chapter audio with chapters return empty asset chapter list`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main audio chapter with chapters return empty asset chapter list`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.AUDIO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main video chapter with mixed audio and video chapters return only video chapters`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val chapter3 = fullLengthChapter.copy(
            urn = "urn:chapter3",
            fullLengthMarkIn = 30,
            fullLengthMarkOut = 60,
            fullLengthUrn = "urn",
            mediaType = MediaType.AUDIO
        )
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2, chapter3))
        assertEquals(
            listOf(chapter1, chapter2).map {
                ChapterAdapter.toChapter(it)
            },
            ChapterAdapter.getChapters(mediaComposition)
        )
    }

    @Test
    fun `main chapter with chapter not related to main chapter are removed`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.EPISODE,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "other urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertEquals(listOf(chapter1).map { ChapterAdapter.toChapter(it) }, ChapterAdapter.getChapters(mediaComposition))
    }

    @Test
    fun `main chapter of type SCHEDULED_LIVESTREAM returns empty chapter list`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.SCHEDULED_LIVESTREAM,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "other urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertTrue(ChapterAdapter.getChapters(mediaComposition).isEmpty())
    }

    @Test
    fun `main chapter of type LIVESTREAM returns empty chapter list`() {
        val fullLengthChapter = Chapter(
            urn = "urn",
            title = "title",
            lead = "lead",
            description = "description",
            imageUrl = "https://www.rts.ch/image.png",
            mediaType = MediaType.VIDEO,
            type = Type.LIVESTREAM,
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapter1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapter2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "other urn")
        val mediaComposition = MediaComposition(chapterUrn = "urn", listChapter = listOf(fullLengthChapter, chapter1, chapter2))
        assertTrue(ChapterAdapter.getChapters(mediaComposition).isEmpty())
    }
}
