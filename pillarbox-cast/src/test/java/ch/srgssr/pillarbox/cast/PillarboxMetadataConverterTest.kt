/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PillarboxMetadataConverterTest {

    @Test
    fun `fromJson from  toJson give the initial result`() {
        val listChapter = listOf(
            Chapter(
                id = "urn:0",
                start = 1000,
                end = 2000,
                title = "Chapter 1",
                artworkUri = "https://example.com/artwork.png",
            ),
            Chapter(
                id = "urn:1",
                start = 1000,
                end = 2000,
                title = "Chapter 2",
            ),
            Chapter(
                id = "urn:2",
                start = 1000,
                end = 2000,
                title = "Chapter 3",
                artworkUri = "https://example.com/artwork.png",
                description = "Description of Chapter 3",
            ),
        )

        val customData = JSONObject()
        PillarboxMetadataConverter.appendChapters(customData, listChapter)
        val parsedChapters = PillarboxMetadataConverter.decodeChapters(customData)
        assertEquals(listChapter, parsedChapters)
    }
}
