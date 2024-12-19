/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class IlHostTest {

    @Test
    fun `parse prod IlHost`() {
        val host = IlHost.parse("https://il.srgssr.ch/somePath/andPath/?withParameters=45")
        assertEquals(IlHost.PROD, host)
    }

    @Test
    fun `parse test IlHost`() {
        val host = IlHost.parse("https://il-test.srgssr.ch/somePath/andPath/?withParameters=45")
        assertEquals(IlHost.TEST, host)
    }

    @Test
    fun `parse stage IlHost`() {
        val host = IlHost.parse("https://il-stage.srgssr.ch/somePath/andPath/?withParameters=45")
        assertEquals(IlHost.STAGE, host)
    }

    @Test
    fun `Check null if invalid hostname`() {
        assertNull(IlHost.parse("https://il-foo.srgssr.ch/somePath/andPath/?withParameters=45"))
        assertNull(IlHost.parse("https://www.google.com/search?q=45"))
    }
}
