/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class QoSSessionTimingsTest {
    @Test
    fun `zero timings`() {
        val timings = QoSSessionTimings.Zero

        assertEquals(Duration.ZERO, timings.asset)
        assertEquals(Duration.ZERO, timings.currentToStart)
        assertEquals(Duration.ZERO, timings.drm)
        assertEquals(Duration.ZERO, timings.mediaSource)
    }
}
