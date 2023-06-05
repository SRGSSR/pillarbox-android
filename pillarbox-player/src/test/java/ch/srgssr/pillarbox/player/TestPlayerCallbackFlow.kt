/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.Player
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestPlayerCallbackFlow {
    private lateinit var player: Player


    @Before
    fun setUp() {
        player = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testCurrentPositionWhilePlaying() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L, 3000L, 4000L, 5000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns true

        val currentPositionFlow = player.currentPositionAsFlow()
        val actualPositions = currentPositionFlow.take(positions.size).toList()
        Assert.assertEquals(positions, actualPositions)
    }

    /**
     * Test current position while not playing
     * We expected a Timeout as the flow doesn't start
     */
    @Test(expected = TimeoutCancellationException::class)
    fun testCurrentPositionWhileNotPlaying() = runTest {
        val positions = listOf(C.TIME_UNSET, 0L, 1000L, 2000L, 3000L, 4000L, 5000L)
        every { player.currentPosition } returnsMany positions
        every { player.isPlaying } returns false

        val currentPositionFlow = player.currentPositionAsFlow()
        val firstPosition = currentPositionFlow.first()
        Assert.assertEquals(positions[0], firstPosition)

        withTimeout(3_000L) {
            val actualPositions = currentPositionFlow.take(positions.size).toList()
            Assert.assertEquals(positions, actualPositions)
        }
    }

}
