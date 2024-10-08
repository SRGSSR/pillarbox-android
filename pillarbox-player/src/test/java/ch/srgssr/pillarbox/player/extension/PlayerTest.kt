/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import io.mockk.every
import io.mockk.mockk
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PlayerTest {
    @Test
    fun `getCurrentMediaItems without any items`() {
        val player = mockk<Player> {
            every { mediaItemCount } returns 0
        }

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
        val player = mockk<Player> {
            every { mediaItemCount } returns mediaItemsCount
            every { getMediaItemAt(any()) } answers { mediaItems[invocation.args[0] as Int] }
        }

        assertEquals(mediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun getPlaybackSpeed() {
        val player = mockk<Player> {
            every { playbackParameters } returns PlaybackParameters(5f)
        }

        assertEquals(5f, player.getPlaybackSpeed())
    }

    @Test
    fun `currentPositionPercentage with negative duration`() {
        val player = mockk<Player> {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns -4L
        }

        assertEquals(-5f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(5f, player.currentPositionPercentage())
    }

    @Test
    fun `currentPositionPercentage with duration equals to 0`() {
        val player = mockk<Player> {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns 0L
        }

        assertEquals(-5f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(5f, player.currentPositionPercentage())
    }

    @Test
    fun `currentPositionPercentage with positive duration`() {
        val player = mockk<Player> {
            every { currentPosition } returnsMany listOf(-5L, 0L, 5L)
            every { duration } returns 4L
        }

        assertEquals(-1.25f, player.currentPositionPercentage())
        assertEquals(0f, player.currentPositionPercentage())
        assertEquals(1.25f, player.currentPositionPercentage())
    }

    @Test
    fun `getCurrentCredits and getCurrentChapters empty without MediaItem`() {
        val player = mockk<Player> {
            every { currentMediaItem } returns null
        }

        assertEquals(emptyList(), player.getCurrentCredits())
        assertEquals(emptyList(), player.getCurrentChapters())
    }

    @Test
    fun `getCurrentCredits, with empty MediaMetadata`() {
        val player = mockk<Player> {
            every { currentMediaItem } returns MediaItem.Builder().build()
        }

        assertEquals(emptyList(), player.getCurrentCredits())
    }

    @Test
    fun `getCurrentCredits, with MediaItem, with credits`() {
        val credits = listOf<Credit>(mockk())
        val player = mockk<Player> {
            every { currentMediaItem } returns MediaItem.Builder()
                .setUri("https://example.com/")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setCredits(credits)
                        .build()
                )
                .build()
        }

        assertEquals(credits, player.getCurrentCredits())
    }

    @Test
    fun `getCurrentChapters, with MediaItem, with chapters`() {
        val chapter = listOf<Chapter>(mockk())
        val player = mockk<Player> {
            every { currentMediaItem } returns MediaItem.Builder()
                .setUri("https://example.com/")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setChapters(chapters = chapter)
                        .build()
                )
                .build()
        }

        assertEquals(chapter, player.getCurrentChapters())
    }

    @Test
    fun `getCurrentChapter, with empty MediaMetadata`() {
        val player = mockk<Player> {
            every { currentMediaItem } returns MediaItem.Builder().build()
        }
        assertEquals(emptyList(), player.getCurrentChapters())
    }
}
