/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.VideoSize
import org.junit.Assert
import org.junit.Test

/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
class TestAspectRatio {

    @Test
    fun testUnknownVideoSize() {
        val input = VideoSize.UNKNOWN
        val expectedAspectRatio = 0.0f
        val unknownAspectRatio = 0.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testUnknownVideoSizeDefaultAspectRatio() {
        val input = VideoSize.UNKNOWN
        val expectedAspectRatio = 1.0f
        val unknownAspectRatio = 1.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testHeightZeroVideoSize() {
        val input = VideoSize(0, 100)
        val expectedAspectRatio = 0.0f
        val unknownAspectRatio = 0.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testWidthZeroVideoSize() {
        val input = VideoSize(240, 0)
        val expectedAspectRatio = 0.0f
        val unknownAspectRatio = 0.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testWidthAndHeightZeroVideoSize() {
        val input = VideoSize(0, 0)
        val expectedAspectRatio = 0.0f
        val unknownAspectRatio = 0.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testAspectRatio16_9() {
        val input = VideoSize(1920, 1080)
        val expectedAspectRatio = 16 / 9f
        val unknownAspectRatio = 1.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testAspectRatio9_16() {
        val input = VideoSize(1080, 1920)
        val expectedAspectRatio = 9 / 16f
        val unknownAspectRatio = 1.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testAspectRatio4_3() {
        val input = VideoSize(800, 600)
        val expectedAspectRatio = 4 / 3f
        val unknownAspectRatio = 1.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }

    @Test
    fun testAspectRatioSquare() {
        val input = VideoSize(500, 500)
        val expectedAspectRatio = 1.0f
        val unknownAspectRatio = 0.0f
        Assert.assertEquals(input.computeAspectRatio(unknownAspectRatio), expectedAspectRatio)
    }
}
