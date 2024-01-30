/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.util.Rational
import androidx.media3.common.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoSizeTest {
    @Test
    fun toRational_unknown_video_size() {
        val input = VideoSize.UNKNOWN
        assertEquals(RATIONAL_ONE, input.toRational())
    }

    @Test
    fun toRational_with_a_16_9_aspect_ratio() {
        val input = VideoSize(1920, 1080)
        assertEquals(Rational(1920, 1080), input.toRational())
    }

    @Test
    fun toRational_with_a_square_aspect_ratio() {
        val input = VideoSize(500, 500)
        assertEquals(Rational(500, 500), input.toRational())
    }
}
