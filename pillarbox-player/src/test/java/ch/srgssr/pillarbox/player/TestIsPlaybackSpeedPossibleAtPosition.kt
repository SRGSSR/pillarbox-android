/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline.EMPTY
import androidx.media3.common.Timeline.Window
import ch.srgssr.pillarbox.player.exoplayer.isPlaybackSpeedPossibleAtPosition
import ch.srgssr.pillarbox.player.test.utils.TestTimeline
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TestIsPlaybackSpeedPossibleAtPosition {

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testEmptyWindow() {
        val window = Window()
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, 0.5f))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, 1.0f))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, 2.0f))
    }

    @Test
    fun testOnDemand() {
        val doubleSpeed = 2.0f
        val normalSpeed = 1.0f
        val halfSpeed = 0.5f
        val duration = 2.hours.inWholeMilliseconds
        val defaultPosition = 0L
        val window = Window()
        setupWindow(window, seekable = false, isLive = false, false, defaultPosition = defaultPosition, duration = duration)

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, doubleSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, doubleSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, doubleSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, doubleSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, doubleSpeed))

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, normalSpeed))

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, halfSpeed))
    }

    @Test
    fun testLiveOnly() {
        val doubleSpeed = 2.0f
        val normalSpeed = 1.0f
        val halfSpeed = 0.5f
        val duration = 1.minutes.inWholeMilliseconds
        val defaultPosition = duration - 30.seconds.inWholeMilliseconds
        val window = Window()
        setupWindow(window, seekable = false, isLive = true, true, defaultPosition = defaultPosition, duration = duration)

        assertFalse(window.isPlaybackSpeedPossibleAtPosition(0, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(duration - 1, doubleSpeed))

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, normalSpeed))

        assertFalse(window.isPlaybackSpeedPossibleAtPosition(0, halfSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, halfSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, halfSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, halfSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(duration - 1, halfSpeed))
    }

    @Test
    fun testLiveDvr() {
        val doubleSpeed = 2.0f
        val normalSpeed = 1.0f
        val halfSpeed = 0.5f
        val duration = 2L.hours.inWholeMilliseconds
        val defaultPosition = duration - 1.hours.inWholeMilliseconds
        val window = Window()
        setupWindow(window, seekable = true, isLive = true, true, defaultPosition = defaultPosition, duration = duration)

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, doubleSpeed))
        assertFalse(window.isPlaybackSpeedPossibleAtPosition(duration - 1, doubleSpeed))

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, normalSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, normalSpeed))

        assertTrue(window.isPlaybackSpeedPossibleAtPosition(0, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition + 1, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(defaultPosition - 1, halfSpeed))
        assertTrue(window.isPlaybackSpeedPossibleAtPosition(duration - 1, halfSpeed))
    }

    @Test
    fun testPlayerEmptyTimeLine() {
        val player: Player = mockk(relaxed = true)
        every { player.currentTimeline } returns EMPTY
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 2.0f))
    }

    @Test
    fun testPlayerOnDemandTimeLine() {
        val player: Player = mockk(relaxed = true)
        val onDemandTimeLine = TestTimeline(isSeekable = true, isLive = false)
        every { player.currentTimeline } returns onDemandTimeLine
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 1.0f))
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 2.0f))
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 0.5f))
    }

    @Test
    fun testPlayerCurrentMediaItemLiveFalse() {
        val player: Player = mockk(relaxed = true)
        val onDemandTimeLine = TestTimeline(isSeekable = true, isLive = false)
        every { player.currentTimeline } returns onDemandTimeLine
        every { player.isCurrentMediaItemLive } returns false
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 1.0f))
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 2.0f))
        assertTrue(player.isPlaybackSpeedPossibleAtPosition(0, 0.5f))
    }

    private fun setupWindow(
        window: Window,
        seekable: Boolean,
        isLive: Boolean,
        isDynamic: Boolean,
        defaultPosition: Long,
        duration: Long = 120_000L
    ) {
        window.set(
            Window.SINGLE_WINDOW_UID,
            null,
            null,
            0, 0, 0,
            seekable,
            isDynamic,
            if (isLive) MediaItem.LiveConfiguration.UNSET else null,
            defaultPosition,
            duration,
            0, 0, 0
        )
    }
}
