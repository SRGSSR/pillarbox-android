/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlin.test.Test
import kotlin.test.assertNull

class QoSSessionTimingsTest {
    @Test
    fun `empty timings`() {
        val timings = QoSSessionTimings.Empty

        assertNull(timings.asset)
        assertNull(timings.drm)
        assertNull(timings.metadata)
        assertNull(timings.total)
    }
}
