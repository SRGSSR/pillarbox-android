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
    fun `encode decode list block segment`() {
        val blockedTimeRanges = listOf(
            BlockedTimeRange(id = "id01", start = 1000, end = 2000, reason = "reason"),
            BlockedTimeRange(id = null, start = 1000, end = 2000, reason = "reason"),
            BlockedTimeRange(id = null, start = 1000, end = 2000, reason = null),
        )
        assertEquals(blockedTimeRanges, json.decodeFromString(json.encodeToString(blockedTimeRanges)))
    }
}
