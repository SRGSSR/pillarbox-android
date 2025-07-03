/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CastChapterAdapterTest {

    @Test
    fun `fromJson from  toJson give the initial result`() {
        val listChapter = listOf(
            Chapter(
                id = "urn:0",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 1")
                    .setArtworkUri("https://example.com/artwork.png".toUri())
                    .build()
            ),
            Chapter(
                id = "urn:1",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setArtworkUri("https://example.com/artwork.png".toUri())
                    .build()
            ),
            Chapter(
                id = "urn:2",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 3")
                    .build()
            ),
        )

        val parsedChapters = CastChapterAdapter.fromJson(CastChapterAdapter.toJson(listChapter))
        assertEquals(listChapter, parsedChapters)
    }
}
