/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PlayerTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var player: Player

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getCurrentMediaItems without any items`() {
        every { player.mediaItemCount } returns 0
        assertEquals(0, player.getCurrentMediaItems().size)
    }

    @Test
    fun `getCurrentMediaItems with some items`() {
        val mediaItemsCount = 3
        val mediaItems = buildList(mediaItemsCount) {
            repeat(mediaItemsCount) {
                add(mockk<MediaItem>())
            }
        }
        player.apply {
            every { mediaItemCount } returns mediaItemsCount
            every { getMediaItemAt(any()) } answers { mediaItems[invocation.args[0] as Int] }
        }

        assertEquals(mediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun getPlaybackSpeed() {
        player.apply {
            every { playbackParameters } returns PlaybackParameters(5f)
        }

        assertEquals(5f, player.getPlaybackSpeed())
    }

    @Test
    fun `currentPositionPercentage with negative duration`() {
        player.apply {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns -4L
        }

        assertEquals(-5f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(5f, player.currentPositionPercentage())
    }

    @Test
    fun `currentPositionPercentage with duration equals to 0`() {
        player.apply {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns 0L
        }

        assertEquals(-5f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(5f, player.currentPositionPercentage())
    }

    @Test
    fun `currentPositionPercentage with positive duration`() {
        player.apply {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns 4L
        }

        assertEquals(-1.25f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(1.25f, player.currentPositionPercentage())
    }
}
