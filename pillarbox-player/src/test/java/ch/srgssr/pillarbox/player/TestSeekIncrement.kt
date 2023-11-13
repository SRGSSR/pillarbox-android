/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class TestSeekIncrement {

    @Test(expected = IllegalArgumentException::class)
    fun testBothZero() {
        SeekIncrement(backward = Duration.ZERO, forward = Duration.ZERO)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBothNegative() {
        SeekIncrement(backward = NegativeIncrement, forward = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekBackNegative() {
        SeekIncrement(backward = NegativeIncrement, forward = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekBackZero() {
        SeekIncrement(backward = ZERO, forward = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekForwardNegative() {
        SeekIncrement(backward = PositiveIncrement, forward = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekForwardZero() {
        SeekIncrement(backward = PositiveIncrement, forward = ZERO)
    }

    @Test
    fun testPositive() {
        val seekBack = 10.seconds
        val seekForward = 15.seconds
        val increment = SeekIncrement(backward = seekBack, forward = seekForward)
        Assert.assertEquals(seekBack, increment.backward)
        Assert.assertEquals(seekForward, increment.forward)
    }


    companion object {
        private val NegativeIncrement = (-10).seconds
        private val PositiveIncrement = 5.seconds
    }
}
