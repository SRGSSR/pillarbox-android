/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import kotlin.test.Test
import kotlin.test.assertEquals

class MediaEventTypeTest {
    @Test
    fun `to string`() {
        assertEquals("play", MediaEventType.Play.toString())
        assertEquals("pause", MediaEventType.Pause.toString())
        assertEquals("eof", MediaEventType.Eof.toString())
        assertEquals("stop", MediaEventType.Stop.toString())
        assertEquals("seek", MediaEventType.Seek.toString())
        assertEquals("pos", MediaEventType.Pos.toString())
        assertEquals("uptime", MediaEventType.Uptime.toString())
    }
}
