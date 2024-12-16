/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl.Companion.toIlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class IlUrnTest {
    @Test
    fun testInOut() {
        val ilUrn = IlUrl(host = IlHost.PROD, urn = "urn:rts:video:12345", vector = Vector.MOBILE)
        assertEquals(ilUrn, ilUrn.uri.toIlUrl())
    }
}
