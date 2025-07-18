/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PillarboxMetadataConverterTest {

    @Test
    fun `chapter fromJson from toJson give the initial result`() {
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

    @Test
    fun `Credit fromJson from toJson give the initial result`() {
        val listCredits = listOf(
            Credit.Opening(
                start = 1000,
                end = 2000,
            ),
            Credit.Closing(
                start = 25000,
                end = 30000,
            ),
        )

        val customData = JSONObject()
        PillarboxMetadataConverter.appendCredits(customData, listCredits)
        val parsedCredits = PillarboxMetadataConverter.decodeCredits(customData)
        assertEquals(listCredits, parsedCredits)
    }

    @Test
    fun `BlockedTimeRanges fromJson from toJson give the initial result`() {
        val listBlockedTimeRanges = listOf(
            BlockedTimeRange(
                start = 0,
                end = 5000,
                reason = "ILLEGAL"
            ),
            BlockedTimeRange(
                start = 10000,
                end = 12000,
                reason = null,
            ),
            BlockedTimeRange(
                start = 10000,
                end = 12000,
                reason = null,
                id = "id"
            )
        )

        val customData = JSONObject()
        PillarboxMetadataConverter.appendBlockedTimeRanges(customData, listBlockedTimeRanges)
        val parsedBlockedTimeRanges = PillarboxMetadataConverter.decodeBlockedTimeRanges(customData)
        assertEquals(listBlockedTimeRanges, parsedBlockedTimeRanges)
    }
}
