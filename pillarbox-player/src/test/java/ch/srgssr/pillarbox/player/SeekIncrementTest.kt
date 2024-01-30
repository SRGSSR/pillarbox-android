/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class SeekIncrementTest {
    @Test(expected = IllegalArgumentException::class)
    fun `both increments are zero`() {
        SeekIncrement(backward = ZERO, forward = ZERO)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `both increments are negative`() {
        SeekIncrement(backward = NegativeIncrement, forward = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `backward increment is negative`() {
        SeekIncrement(backward = NegativeIncrement, forward = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `backward increment is zero`() {
        SeekIncrement(backward = ZERO, forward = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `forward increment is negative`() {
        SeekIncrement(backward = PositiveIncrement, forward = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `forward increment is zero`() {
        SeekIncrement(backward = PositiveIncrement, forward = ZERO)
    }

    @Test
    fun `both increments are positive`() {
        val seekBack = 10.seconds
        val seekForward = 15.seconds
        val increment = SeekIncrement(backward = seekBack, forward = seekForward)
        assertEquals(seekBack, increment.backward)
        assertEquals(seekForward, increment.forward)
    }

    companion object {
        private val NegativeIncrement = (-10).seconds
        private val PositiveIncrement = 5.seconds
    }
}
