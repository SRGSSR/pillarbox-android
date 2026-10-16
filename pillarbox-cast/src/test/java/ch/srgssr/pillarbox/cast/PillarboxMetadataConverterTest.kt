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
import ch.srgssr.pillarbox.cast.data.CastChapter
import ch.srgssr.pillarbox.cast.data.CastTimeRange
import ch.srgssr.pillarbox.cast.data.PillarboxCastCustomData
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PillarboxMetadataConverterTest {

    @Test
    fun `decode web receiver custom data`() {
        val customData = JSONObject(
            """
            {
                "server": "il.srgssr.ch",
                "blockedSegments": [
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
                "timeRanges": [
                    {
                        "startTime": 1512200,
                        "endTime": 1592600,
                        "type": "CLOSING_CREDITS"
                    }
                ]
            }
            """.trimIndent()
        )

        val expected = PillarboxCastCustomData(
            chapters = listOf(
                CastChapter(
                    startTime = 81000,
                    endTime = 170800,
                    identifier = "urn:rts:video:14827730",
                    posterUrl = "https://img.rts.ch/medias/2024/image/elrzeb-28465583.image/16x9",
                    title = "Chapter 1",
                ),
                CastChapter(startTime = 193800, endTime = 329600, identifier = "urn:rts:video:14827732"),
            ),
            blockedSegments = listOf(
                CastBlockedSegment(
                    startTime = 1766438,
                    endTime = 1973715,
                    identifier = "urn:srf:video:84135f7b-c58d-4a2d-b0b0-e8680581eede",
                    blockReason = "LEGAL",
                ),
            ),
            timeRanges = listOf(
                CastTimeRange(startTime = 1512200, endTime = 1592600, type = CastTimeRange.CLOSING_CREDITS),
            ),
        )
        assertEquals(expected, PillarboxMetadataConverter.decodeCustomData(customData))
    }

    @Test
    fun `decode without metadata keys returns empty metadata`() {
        val decodedData = PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"server": "il.srgssr.ch"}"""))

        assertEquals(PillarboxCastCustomData(), decodedData)
        assertEquals(PillarboxMetadata.EMPTY, decodedData.toPillarboxMetadata())
    }

    @Test
    fun `decode returns empty chapters with invalid chapters`() {
        val decodedData = PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"chapters": [{"title": "missing times"}]}"""))

        assertTrue(decodedData.chapters.isEmpty())
    }

    @Test
    fun `decode returns empty time ranges with invalid time ranges`() {
        val decodedData = PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"timeRanges": [{"title": "missing times"}]}"""))

        assertTrue(decodedData.timeRanges.isEmpty())
    }

    @Test
    fun `decode returns empty blocked segments with invalid blocked segments`() {
        val decodedData = PillarboxMetadataConverter.decodeCustomData(JSONObject("""{"blockedSegments": [{"title": "missing times"}]}"""))

        assertTrue(decodedData.blockedSegments.isEmpty())
    }

    @Test
    fun `decode keeps valid keys when another key is invalid`() {
        val customData = JSONObject(
            """
            {
                "chapters": [
                    {
                        "startTime": 0,
                        "endTime": 1000,
                        "identifier": "urn:rts:video:1"
                    }
                ],
                "timeRanges": [
                    {
                        "type": "OPENING_CREDITS"
                    }
                ]
            }
            """.trimIndent()
        )

        val decodedData = PillarboxMetadataConverter.decodeCustomData(customData)

        assertEquals(listOf(CastChapter(startTime = 0, endTime = 1000, identifier = "urn:rts:video:1")), decodedData.chapters)
        assertTrue(decodedData.timeRanges.isEmpty())
    }

    @Test
    fun `unknown time range types are ignored`() {
        val customData = PillarboxCastCustomData(
            timeRanges = listOf(
                CastTimeRange(startTime = 0, endTime = 1000, type = CastTimeRange.OPENING_CREDITS),
                CastTimeRange(startTime = 1000, endTime = 2000, type = "UNKNOWN"),
                CastTimeRange(startTime = 2000, endTime = 3000),
            ),
        )

        assertEquals(listOf(Credit.Opening(start = 0, end = 1000)), customData.toPillarboxMetadata().credits)
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
        val blockedTimeRanges = listOf(
            BlockedTimeRange(start = 0, end = 1000, reason = "LEGAL", id = "urn:rts:video:3"),
        )
        val credits = listOf(
            Credit.Opening(start = 0, end = 1000),
            Credit.Closing(start = 30000, end = 30500),
        )
        val metadata = PillarboxMetadata(chapters = chapters, blockedTimeRanges = blockedTimeRanges, credits = credits)
        val customData = JSONObject().put("server", "il.srgssr.ch")

        metadata.appendToCustomData(customData)

        assertEquals("il.srgssr.ch", customData.getString("server"))
        assertEquals(metadata, PillarboxMetadataConverter.decodeCustomData(customData).toPillarboxMetadata())
    }

    @Test
    fun `append empty metadata removes previous values`() {
        val customData = JSONObject().put("server", "il.srgssr.ch")
        PillarboxMetadata(
            chapters = listOf(Chapter(id = "urn:rts:video:1", start = 0, end = 1000, mediaMetadata = MediaMetadata.EMPTY)),
            blockedTimeRanges = listOf(BlockedTimeRange(start = 0, end = 1000)),
            credits = listOf(Credit.Opening(start = 0, end = 1000)),
        ).appendToCustomData(customData)

        PillarboxMetadata.EMPTY.appendToCustomData(customData)

        assertEquals("il.srgssr.ch", customData.getString("server"))
        assertFalse(customData.has(PillarboxMetadataConverter.KEY_CHAPTERS))
        assertFalse(customData.has(PillarboxMetadataConverter.KEY_BLOCKED_SEGMENTS))
        assertFalse(customData.has(PillarboxMetadataConverter.KEY_TIME_RANGES))
    }
}
