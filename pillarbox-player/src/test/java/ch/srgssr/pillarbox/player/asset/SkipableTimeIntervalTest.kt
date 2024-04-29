/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SkipableTimeIntervalTest {
    @Test
    fun `SkipableTimeInterval#type with empty id`() {
        val timeInterval = SkipableTimeInterval(
            id = "",
            start = 0L,
            end = 10L,
        )

        assertNull(timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with unknown id`() {
        val timeInterval = SkipableTimeInterval(
            id = "CLOSING",
            start = 0L,
            end = 10L,
        )

        assertNull(timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with id=CLOSING_CREDITS`() {
        val timeInterval = SkipableTimeInterval(
            id = "CLOSING_CREDITS",
            start = 0L,
            end = 10L,
        )

        assertEquals(SkipableTimeIntervalType.CLOSING_CREDITS, timeInterval.type)
    }

    @Test
    fun `SkipableTimeInterval#type with id=OPENING_CREDITS`() {
        val timeInterval = SkipableTimeInterval(
            id = "OPENING_CREDITS",
            start = 0L,
            end = 10L,
        )

        assertEquals(SkipableTimeIntervalType.OPENING_CREDITS, timeInterval.type)
    }
}
