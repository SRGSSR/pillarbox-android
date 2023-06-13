/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
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
        SeekIncrement(seekBackIncrement = Duration.ZERO, seekForwardIncrement = Duration.ZERO)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBothNegative() {
        SeekIncrement(seekBackIncrement = NegativeIncrement, seekForwardIncrement = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekBackNegative() {
        SeekIncrement(seekBackIncrement = NegativeIncrement, seekForwardIncrement = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekBackZero() {
        SeekIncrement(seekBackIncrement = ZERO, seekForwardIncrement = PositiveIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekForwardNegative() {
        SeekIncrement(seekBackIncrement = PositiveIncrement, seekForwardIncrement = NegativeIncrement)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSeekForwardZero() {
        SeekIncrement(seekBackIncrement = PositiveIncrement, seekForwardIncrement = ZERO)
    }

    @Test
    fun testPositive() {
        val seekBack = 10.seconds
        val seekForward = 15.seconds
        val increment = SeekIncrement(seekBackIncrement = seekBack, seekForwardIncrement = seekForward)
        Assert.assertEquals(seekBack, increment.seekBackIncrement)
        Assert.assertEquals(seekForward, increment.seekForwardIncrement)
    }


    companion object {
        private val NegativeIncrement = (-10).seconds
        private val PositiveIncrement = 5.seconds
    }
}
