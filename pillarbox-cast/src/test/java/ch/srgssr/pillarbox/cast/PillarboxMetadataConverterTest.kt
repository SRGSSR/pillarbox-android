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
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import org.json.JSONObject
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PillarboxMetadataConverterTest {
    @Test
    fun `PillarboxMetadata encode and decode give the initial result`() {
        val listChapter = listOf(
            Chapter(
                id = "urn:0",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 1")
                    .setArtworkUri(
                        """
                        https://example.com/artwork.png
                        """.trimIndent().toUri()
                    )
                    .build()
            ),
            Chapter(
                id = "urn:1",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 2")
                    .build()
            ),
            Chapter(
                id = "urn:2",
                start = 1000,
                end = 2000,
                mediaMetadata = MediaMetadata.Builder()
                    .setTitle("Chapter 3")
                    .setDescription("Description of Chapter 3")
                    .setArtworkUri(
                        """
                        https://example.com/artwork.png
                        """.trimIndent().toUri()
                    )
                    .build()
            ),
        )
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
        val pillarboxMetadata = PillarboxMetadata(
            chapters = listChapter,
            credits = listCredits,
            blockedTimeRanges = listBlockedTimeRanges
        )

        val customData = JSONObject()
        customData.put("customField", "data")
        pillarboxMetadata.appendToCustomData(customData)
        assertTrue(customData.has(PillarboxMetadataConverter.KEY_PILLARBOX))
        assertEquals("data", customData.get("customField"))
        val parsedMetadata = PillarboxMetadataConverter.decodePillarboxMetadata(customData)
        assertEquals(pillarboxMetadata, parsedMetadata)
    }
}
