/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeIntervalType
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter.toSkipableTimeInterval
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimeIntervalAdapterTest {

    @Test
    fun `empty time interval produce null Credit`() {
        val timeInterval = TimeInterval(markIn = null, markOut = null, type = null)
        assertNull(timeInterval.toSkipableTimeInterval())
    }

    @Test
    fun `null markOut time interval produce null Credit`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = null, type = TimeIntervalType.CLOSING_CREDITS)
        assertNull(timeInterval.toSkipableTimeInterval())
    }

    @Test
    fun `null markIn time interval produce null Credit`() {
        val timeInterval = TimeInterval(markIn = null, markOut = 100, type = TimeIntervalType.CLOSING_CREDITS)
        assertNull(timeInterval.toSkipableTimeInterval())
    }

    @Test
    fun `null type time interval produce null Credit`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = null)
        assertNull(timeInterval.toSkipableTimeInterval())
    }

    @Test
    fun `OPENING_CREDITS type time interval produce null Opening`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = TimeIntervalType.OPENING_CREDITS)
        assertEquals(Credit.Opening(start = 100, end = 200), timeInterval.toSkipableTimeInterval())
    }

    @Test
    fun `CLOSING_CREDITS type time interval produce null Opening`() {
        val timeInterval = TimeInterval(markIn = 100, markOut = 200, type = TimeIntervalType.CLOSING_CREDITS)
        assertEquals(Credit.Closing(start = 100, end = 200), timeInterval.toSkipableTimeInterval())
    }
}
