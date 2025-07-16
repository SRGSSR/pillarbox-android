/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class JsonTimeRangesTest {
    val json = jsonTimeRanges

    @Test
    fun `encode credit with polymorphism`() {
        val openingCredit: Credit = Credit.Opening(1000, 2000)
        val closingCredit: Credit = Credit.Closing(500, 5000)
        val openingCreditJson = json.encodeToString(openingCredit)
        val closingCreditJson = json.encodeToString(closingCredit)
        val expectedOpeningCreditJson = """{"type":"OpeningCredit","start":1000,"end":2000}"""
        val expectedClosingCreditJson = """{"type":"ClosingCredit","start":500,"end":5000}"""
        assertEquals(expectedOpeningCreditJson, openingCreditJson)
        assertEquals(expectedClosingCreditJson, closingCreditJson)
    }

    @Test
    fun `encode credit`() {
        val openingCredit = Credit.Opening(1000, 2000)
        val closingCredit = Credit.Closing(500, 5000)
        val openingCreditJson = json.encodeToString(openingCredit)
        val closingCreditJson = json.encodeToString(closingCredit)
        val expectedOpeningCreditJson = """{"start":1000,"end":2000}"""
        val expectedClosingCreditJson = """{"start":500,"end":5000}"""
        assertEquals(expectedOpeningCreditJson, openingCreditJson)
        assertEquals(expectedClosingCreditJson, closingCreditJson)
    }

    @Test
    fun `encode decode list credit`() {
        val credits: List<Credit> = listOf(
            Credit.Opening(1000, 2000),
            Credit.Closing(500, 5000)
        )

        credits.forEach {
            val creditJson: Credit = json.decodeFromString(json.encodeToString(it))
            assertEquals(it, creditJson)
        }
        assertEquals(credits, json.decodeFromString(json.encodeToString(credits)))
    }

    @Test
    fun `encode chapter`() {
        val chapter = Chapter(id = "id:1", start = 1000, end = 2000, title = "title", description = null, artworkUri = "https://image.png")
        val chapterJson = json.encodeToString(chapter)
        val expectedChapterJson = """{"id":"id:1","start":1000,"end":2000,"title":"title","artworkUri":"https://image.png"}"""
        assertEquals(expectedChapterJson, chapterJson)
    }

    @Test
    fun `encode decode chapter`() {
        val chapters = listOf(
            Chapter(id = "id:1", start = 1000, end = 2000, title = "title 1", description = null, artworkUri = null),
            Chapter(id = "id:2", start = 2000, end = 3000, title = "title 2", description = "The description", artworkUri = null),
            Chapter(id = "id:3", start = 4000, end = 10000, title = "title 3", description = null, artworkUri = "https://image.png"),
            Chapter(id = "id:4", start = 4000, end = 10000, title = "title 4", description = "The description", artworkUri = "https://image.png"),
        )
        assertEquals(chapters, json.decodeFromString(json.encodeToString(chapters)))
    }

    @Test
    fun `encode decode list block segment`() {
        val blockedTimeRanges = listOf(
            BlockedTimeRange(id = "id01", start = 1000, end = 2000, reason = "reason"),
            BlockedTimeRange(id = null, start = 1000, end = 2000, reason = "reason"),
            BlockedTimeRange(id = null, start = 1000, end = 2000, reason = null),
        )
        assertEquals(blockedTimeRanges, json.decodeFromString(json.encodeToString(blockedTimeRanges)))
    }
}
