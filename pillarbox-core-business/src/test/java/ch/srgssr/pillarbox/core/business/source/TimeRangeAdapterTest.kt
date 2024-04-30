package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeIntervalType
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter.toSkipableTimeInterval
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeRangeAdapterTest {
    @Test
    fun `get time intervals, source is null`() {
        val timeIntervals = TimeIntervalAdapter.getCredits(null)

        assertTrue(timeIntervals.isEmpty())
    }

    @Test
    fun `get time intervals, source is empty`() {
        val timeIntervals = TimeIntervalAdapter.getCredits(emptyList())

        assertTrue(timeIntervals.isEmpty())
    }

    @Test
    fun `get time intervals, source is not empty`() {
        val originalTimeIntervals = listOf(
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
        val timeIntervals = TimeIntervalAdapter.getCredits(originalTimeIntervals)
        val expectedTimeIntervals = listOf(
            originalTimeIntervals[0].toSkipableTimeInterval(),
            originalTimeIntervals[1].toSkipableTimeInterval(),
        )

        assertEquals(expectedTimeIntervals, timeIntervals)
    }
}
