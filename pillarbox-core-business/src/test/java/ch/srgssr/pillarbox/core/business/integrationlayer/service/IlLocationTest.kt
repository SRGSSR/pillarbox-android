/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IlLocationTest {
    @Test
    fun `fromName CH`() {
        assertEquals(IlLocation.CH, IlLocation.fromName("ch"))
        assertEquals(IlLocation.CH, IlLocation.fromName("CH"))
    }

    @Test
    fun `fromName WW`() {
        assertEquals(IlLocation.WW, IlLocation.fromName("ww"))
        assertEquals(IlLocation.WW, IlLocation.fromName("WW"))
    }

    @Test
    fun `fromName invalid name`() {
        assertNull(IlLocation.fromName("INVALID"))
    }
}
