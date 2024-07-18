/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import ch.srgssr.pillarbox.player.analytics.TotalPlaytimeCounter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TotalPlaytimeCounterTest {
    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `total playtime counter`() = runTest {
        advanceTimeBy(10.seconds)

        val counter = TotalPlaytimeCounter { currentTime }
        assertEquals(0.milliseconds, counter.getTotalPlayTime())

        counter.play()
        advanceTimeBy(5.seconds)
        assertEquals(5.seconds, counter.getTotalPlayTime())

        counter.pause()
        advanceTimeBy(5.seconds)
        assertEquals(5.seconds, counter.getTotalPlayTime())

        counter.play()
        advanceTimeBy(5.seconds)
        assertEquals(10.seconds, counter.getTotalPlayTime())

        counter.pause()
        advanceTimeBy(5.seconds)
        assertEquals(10.seconds, counter.getTotalPlayTime())
    }
}
