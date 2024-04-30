/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SkipableTimeRangeTest {
    @Test
    fun `SkipableTimeInterval#type with empty id`() {
        val timeInterval = SkipableTimeRange(
            id = "",
            start = 0L,
            end = 10L,
        )

        assertNull(timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with unknown id`() {
        val timeInterval = SkipableTimeRange(
            id = "CLOSING",
            start = 0L,
            end = 10L,
        )

        assertNull(timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with id=CLOSING_CREDITS`() {
        val timeInterval = SkipableTimeRange(
            id = "CLOSING_CREDITS",
            start = 0L,
            end = 10L,
        )

        assertEquals(SkipableTimeRangeType.CLOSING_CREDITS, timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with id=OPENING_CREDITS`() {
        val timeInterval = SkipableTimeRange(
            id = "OPENING_CREDITS",
            start = 0L,
            end = 10L,
        )

        assertEquals(SkipableTimeRangeType.OPENING_CREDITS, timeInterval.type)
    }
}
