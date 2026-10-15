/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.cast.PillarboxMetadataConverter.appendToCustomData
import ch.srgssr.pillarbox.cast.data.CastBlockedSegment
import ch.srgssr.pillarbox.cast.data.CastTimeRange
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
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
                    "blockedSegments":
                    [
                        {
                            "startTime": 1766438,
                            "endTime": 1973715,
                            "identifier": "urn:srf:video:84135f7b-c58d-4a2d-b0b0-e8680581eede",
                            "blockReason": "LEGAL"
                        }
                    ],
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
                ],
    "timeRanges":
    [
        {"startTime":1512200,"endTime":1592600,"type":"CLOSING_CREDITS"}
    ]
            }
            """.trimIndent()
        )

        val expectedChapters = listOf(
            CastChapter(
                startTime = 81000,
                endTime = 170800,
                identifier = "urn:rts:video:14827730",
                posterUrl = "https://img.rts.ch/medias/2024/image/elrzeb-28465583.image/16x9",
                title = "Chapter 1",
            ),
            CastChapter(startTime = 193800, endTime = 329600, identifier = "urn:rts:video:14827732"),
        )

        val expectedBlockedSegment = listOf(
            CastBlockedSegment(
                startTime = 1766438,
                endTime = 1973715,
                identifier = "urn:srf:video:84135f7b-c58d-4a2d-b0b0-e8680581eede",
                blockReason = "LEGAL",
            ),
        )

        val expectedTimeRanges = listOf(
            CastTimeRange(
                startTime = 1512200,
                endTime = 1592600,
                type = "CLOSING_CREDITS"
            ),
        )
        val decodedData = PillarboxMetadataConverter.decodeCustomData(customData)
        assertEquals(expectedChapters, decodedData?.chapters)
        assertEquals(expectedBlockedSegment, decodedData?.blockedSegments)
        assertEquals(expectedTimeRanges, decodedData?.timeRanges)
    }

    @Test
    fun `decode returns null without custom data`() {
        val decodedData = PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"server": "il.srgssr.ch"}"""))
        assertNull(decodedData?.chapters)
        assertNull(decodedData?.timeRanges)
        assertNull(decodedData?.blockedSegments)
    }

    @Test
    fun `decode returns null with invalid chapters`() {
        assertNull(PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"chapters": [{"title": "missing times"}]}"""))?.chapters)
    }

    @Test
    fun `decode returns null with invalid time ranges`() {
        assertNull(PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"timeRanges": [{"title": "missing times"}]}"""))?.timeRanges)
    }

    @Test
    fun `decode returns null with invalid blocked segments`() {
        assertNull(PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"blockedSegments": [{"title": "missing times"}]}"""))?.blockedSegments)
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
        val blockedSegments = listOf(
            BlockedTimeRange(
                start = 0,
                end = 1000,
                reason = "LEGAL",
                id = "ttt",
            ),
        )
        val credits = listOf(
            Credit.Opening(
                start = 0,
                end = 1000,
            ),
            Credit.Closing(
                start = 30000,
                end = 30500,
            ),
        )
        val customData = JSONObject().put("server", "il.srgssr.ch")

        PillarboxMetadata(chapters = chapters, blockedTimeRanges = blockedSegments, credits = credits).appendToCustomData(customData)

        assertEquals("il.srgssr.ch", customData.getString("server"))
        val decodedCustomData = PillarboxMetadataConverter.decodeCustomData(customData)?.toPillarboxMetadata()
        assertEquals(chapters, decodedCustomData?.chapters)
        assertEquals(blockedSegments, decodedCustomData?.blockedTimeRanges)
        assertEquals(credits, decodedCustomData?.credits)
    }
}
