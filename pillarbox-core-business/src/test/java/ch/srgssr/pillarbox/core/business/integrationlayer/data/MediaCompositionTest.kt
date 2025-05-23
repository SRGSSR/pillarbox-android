/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaCompositionTest {
    @Test
    fun `get main chapter`() {
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:rts:video:8841634",
            listChapter = listOf(
                Chapter(
                    urn = "urn:rts:audio:3262363",
                    title = "Dvr",
                    imageUrl = "https://dvr.image/",
                    mediaType = MediaType.AUDIO,
                    type = Type.EPISODE,
                ),
                Chapter(
                    urn = "urn:rts:video:8841634",
                    title = "Live",
                    imageUrl = "https://live.image/",
                    mediaType = MediaType.VIDEO,
                    type = Type.EPISODE,
                ),
                Chapter(
                    urn = "urn:rts:video:13444428",
                    title = "Short",
                    imageUrl = "https://short.image/",
                    mediaType = MediaType.VIDEO,
                    type = Type.EPISODE,
                ),
            ),
        )
        val mainChapter = mediaComposition.mainChapter

        assertEquals(mediaComposition.listChapter[1], mainChapter)
    }

    @Test(expected = NullPointerException::class)
    fun `get main chapter with empty chapter list`() {
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:rts:video:8841634",
            listChapter = emptyList(),
        )
        mediaComposition.mainChapter
    }

    @Test
    fun `find chapter by urn`() {
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:rts:video:8841634",
            listChapter = listOf(
                Chapter(
                    urn = "urn:rts:audio:3262363",
                    title = "Dvr",
                    imageUrl = "https://dvr.image/",
                    mediaType = MediaType.AUDIO,
                    type = Type.EPISODE,
                ),
                Chapter(
                    urn = "urn:rts:video:8841634",
                    title = "Live",
                    imageUrl = "https://live.image/",
                    mediaType = MediaType.VIDEO,
                    type = Type.EPISODE,
                ),
                Chapter(
                    urn = "urn:rts:video:13444428",
                    title = "Short",
                    imageUrl = "https://short.image/",
                    mediaType = MediaType.VIDEO,
                    type = Type.EPISODE,
                ),
            ),
        )
        val chapter = mediaComposition.findChapterByUrn(mediaComposition.chapterUrn)

        assertEquals(mediaComposition.listChapter[1], chapter)
    }

    @Test
    fun `find chapter by urn with empty chapter list`() {
        val mediaComposition = MediaComposition(
            chapterUrn = "urn:rts:video:8841634",
            listChapter = emptyList(),
        )
        val chapter = mediaComposition.findChapterByUrn(mediaComposition.chapterUrn)

        assertNull(chapter)
    }
}
