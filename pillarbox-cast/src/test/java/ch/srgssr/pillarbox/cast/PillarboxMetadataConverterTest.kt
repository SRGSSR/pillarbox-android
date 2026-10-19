/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.cast.PillarboxMetadataConverter.appendToCustomData
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PillarboxMetadataConverterTest {

    @Test
    fun `decode chapters from web receiver custom data`() {
        val customData = JSONObject(
            """
            {
                "server": "il.srgssr.ch",
                "chapters": [
                    {
                        "startTime": 81000,
                        "endTime": 170800,
                        "identifier": "urn:rts:video:14827730",
                        "posterUrl": "https://img.rts.ch/medias/2024/image/elrzeb-28465583.image/16x9",
                        "title": "Chapter 1"
                    },
                    {
                        "startTime": 193800,
                        "endTime": 329600,
                        "identifier": "urn:rts:video:14827732",
                        "unknownKey": true
                    }
                ]
            }
            """.trimIndent()
        )

        val expected = listOf(
            CastChapter(
                startTime = 81000,
                endTime = 170800,
                identifier = "urn:rts:video:14827730",
                posterUrl = "https://img.rts.ch/medias/2024/image/elrzeb-28465583.image/16x9",
                title = "Chapter 1",
            ),
            CastChapter(startTime = 193800, endTime = 329600, identifier = "urn:rts:video:14827732"),
        )
        assertEquals(expected, PillarboxMetadataConverter.decodeCastChapters(customData))
    }

    @Test
    fun `decode returns null without chapters`() {
        assertNull(PillarboxMetadataConverter.decodeCastChapters(JSONObject("""{"server": "il.srgssr.ch"}""")))
    }

    @Test
    fun `decode returns null with invalid chapters`() {
        assertNull(PillarboxMetadataConverter.decodeCastChapters(JSONObject("""{"chapters": [{"title": "missing times"}]}""")))
    }

    @Test
    fun `append then decode round trip keeps existing keys`() {
        val chapters = listOf(
            Chapter(
                id = "urn:rts:video:1",
                start = 0,
                end = 1000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 1")
                    .setArtworkUri("https://img.rts.ch/image.jpg".toUri())
                    .build(),
            ),
            Chapter(id = "urn:rts:video:2", start = 1000, end = 2000, mediaMetadata = MediaMetadata.EMPTY),
        )
        val customData = JSONObject().put("server", "il.srgssr.ch")

        PillarboxMetadata(chapters = chapters).appendToCustomData(customData)

        assertEquals("il.srgssr.ch", customData.getString("server"))
        val decodedChapters = PillarboxMetadataConverter.decodeCastChapters(customData)?.map(CastChapter::toChapter)
        assertEquals(chapters, decodedChapters)
    }
}
