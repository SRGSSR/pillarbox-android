/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class QoSSessionTimingsTest {
    @Test
    fun `zero timings`() {
        val timings = QoSSessionTimings.Zero

        assertEquals(Duration.ZERO, timings.asset)
        assertEquals(Duration.ZERO, timings.drm)
        assertEquals(Duration.ZERO, timings.mediaSource)
        assertEquals(Duration.ZERO, timings.total)
    }

    @Test
    fun `timings compute total value`() {
        val timings = QoSSessionTimings(
            asset = 250.milliseconds,
            drm = 33.milliseconds,
            mediaSource = 100.milliseconds,
        )

        assertEquals(383.milliseconds, timings.total)
    }
}
