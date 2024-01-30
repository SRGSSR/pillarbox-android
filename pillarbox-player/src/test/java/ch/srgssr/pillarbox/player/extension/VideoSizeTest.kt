/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.VideoSize
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoSizeTest {
    @Test
    fun `computeAspectRatio unknown video size`() {
        val input = VideoSize.UNKNOWN
        val expectedAspectRatio = 16f / 9
        val unknownAspectRatio = 16f / 9
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with height set to 0`() {
        val input = VideoSize(0, 100)
        val expectedAspectRatio = 16f / 9
        val unknownAspectRatio = 16f / 9
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with width set to 0`() {
        val input = VideoSize(240, 0)
        val expectedAspectRatio = 16f / 9
        val unknownAspectRatio = 16f / 9
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with width and height set to 0`() {
        val input = VideoSize(0, 0)
        val expectedAspectRatio = 16f / 9
        val unknownAspectRatio = 16f / 9
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with a 16-9 aspect ratio`() {
        val input = VideoSize(1920, 1080)
        val expectedAspectRatio = 16f / 9
        val unknownAspectRatio = 1f
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with a 9-16 aspect ratio`() {
        val input = VideoSize(1080, 1920)
        val expectedAspectRatio = 9f / 16
        val unknownAspectRatio = 1f
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with a 4-3 aspect ratio`() {
        val input = VideoSize(800, 600)
        val expectedAspectRatio = 4f / 3
        val unknownAspectRatio = 1f
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }

    @Test
    fun `computeAspectRatio with a square aspect ratio`() {
        val input = VideoSize(500, 500)
        val expectedAspectRatio = 1f
        val unknownAspectRatio = 0f
        assertEquals(expectedAspectRatio, input.computeAspectRatio(unknownAspectRatio))
    }
}