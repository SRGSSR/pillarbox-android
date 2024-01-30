/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ExoPlayerBuilderTest {
    @Test
    fun `set seek increments`() {
        val exoPlayerBuilder = mockk<ExoPlayer.Builder> {
            every { setSeekBackIncrementMs(any()) } returns this
            every { setSeekForwardIncrementMs(any()) } returns this
        }
        val seekIncrement = SeekIncrement(
            backward = 45.seconds,
            forward = 180.seconds,
        )

        exoPlayerBuilder.setSeekIncrements(seekIncrement)

        verify(exactly = 1) {
            exoPlayerBuilder.setSeekBackIncrementMs(seekIncrement.backward.inWholeMilliseconds)
            exoPlayerBuilder.setSeekForwardIncrementMs(seekIncrement.forward.inWholeMilliseconds)
        }
        confirmVerified(exoPlayerBuilder)
    }
}
