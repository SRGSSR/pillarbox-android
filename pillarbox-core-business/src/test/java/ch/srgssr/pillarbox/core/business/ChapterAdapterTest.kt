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
import ch.srgssr.pillarbox.core.business.source.ChapterAdapter
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

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
        )
        val expected = ch.srgssr.pillarbox.player.asset.Chapter(
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
        )
        val mediaComposition = MediaComposition(
            chapterUrn = mainChapter.urn, listChapter = listOf(mainChapter)
        )
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
        )
        val chapter1 = mainChapter.copy(urn = "urn:chapitre1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = mainChapter.copy(urn = "urn:chapitre2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = mainChapter.urn, listChapter = listOf(mainChapter, chapter1, chapter2)
        )
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
        )
        val chapter1 = fullLengthChapter.copy(urn = "urn:chapitre1", fullLengthMarkIn = 0, fullLengthMarkOut = 10, fullLengthUrn = "urn")
        val chapter2 = fullLengthChapter.copy(urn = "urn:chapitre2", fullLengthMarkIn = 30, fullLengthMarkOut = 60, fullLengthUrn = "urn")
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:chapitre1", listChapter = listOf(fullLengthChapter, chapter1, chapter2)
        )
        assertEquals(emptyList(), ChapterAdapter.getChapters(mediaComposition))
    }
}
