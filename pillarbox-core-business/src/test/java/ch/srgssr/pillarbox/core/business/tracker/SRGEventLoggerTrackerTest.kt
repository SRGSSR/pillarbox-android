/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class SRGEventLoggerTrackerTest {
    @Test
    fun `event logger`() {
        val player = mockk<ExoPlayer>(relaxed = true)
        val eventLogger = SRGEventLoggerTracker.Factory().create()

        eventLogger.start(player, Unit)
        eventLogger.stop(player)

        verifySequence {
            player.addAnalyticsListener(any())
            player.removeAnalyticsListener(any())
        }
    }
}
