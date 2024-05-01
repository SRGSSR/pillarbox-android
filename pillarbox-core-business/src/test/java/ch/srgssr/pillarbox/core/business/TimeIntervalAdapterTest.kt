/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeIntervalType
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter.toCredit
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeIntervalAdapterTest {

    @Test
    fun `get credits, source is null`() {
        val credits = TimeIntervalAdapter.getCredits(null)

        assertTrue(credits.isEmpty())
    }

    @Test
    fun `get credits, source is empty`() {
        val credits = TimeIntervalAdapter.getCredits(emptyList())

        assertTrue(credits.isEmpty())
    }

    @Test
    fun `get credits, source is not empty`() {
        val timeIntervals = listOf(
            // Valid time intervals
            TimeInterval(markIn = 10L, markOut = 20L, type = TimeIntervalType.OPENING_CREDITS),
            TimeInterval(markIn = 30L, markOut = 100L, type = TimeIntervalType.CLOSING_CREDITS),

            // Invalid time intervals
            TimeInterval(markIn = null, markOut = null, type = null),
            TimeInterval(markIn = 10L, markOut = null, type = null),
            TimeInterval(markIn = 10L, markOut = 20L, type = null),
            TimeInterval(markIn = 10L, markOut = null, type = TimeIntervalType.OPENING_CREDITS),
            TimeInterval(markIn = null, markOut = 20L, type = null),
            TimeInterval(markIn = null, markOut = 20L, type = TimeIntervalType.CLOSING_CREDITS),
            TimeInterval(markIn = null, markOut = null, type = TimeIntervalType.OPENING_CREDITS),
        )
        val credits = TimeIntervalAdapter.getCredits(timeIntervals)
        val expectedCredits = listOf(
            timeIntervals[0].toCredit(),
            timeIntervals[1].toCredit(),
        )

        assertEquals(expectedCredits, credits)
    }

    @Test
    fun `empty time interval produces null Credit`() {
        val timeInterval = TimeInterval(markIn = null, markOut = null, type = null)
        assertNull(timeInterval.toCredit())
    }

    @Test
    fun `null markOut produces null Credit`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = null, type = TimeIntervalType.CLOSING_CREDITS)
        assertNull(timeInterval.toCredit())
    }

    @Test
    fun `null markIn produces null Credit`() {
        val timeInterval = TimeInterval(markIn = null, markOut = 100, type = TimeIntervalType.CLOSING_CREDITS)
        assertNull(timeInterval.toCredit())
    }

    @Test
    fun `null type produces null Credit`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = null)
        assertNull(timeInterval.toCredit())
    }

    @Test
    fun `OPENING_CREDITS type produces Opening`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = TimeIntervalType.OPENING_CREDITS)
        assertEquals(Credit.Opening(start = 100, end = 200), timeInterval.toCredit())
    }

    @Test
    fun `CLOSING_CREDITS type produces Opening`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = TimeIntervalType.CLOSING_CREDITS)
        assertEquals(Credit.Closing(start = 100, end = 200), timeInterval.toCredit())
    }
}
