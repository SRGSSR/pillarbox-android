/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl.Companion.toIlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class IlUrlTest {

    @Test(expected = IllegalArgumentException::class)
    fun `toIlUrl throws an error when not an url`() {
        Uri.parse("yolo").toIlUrl()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toIlUrl throws an error with invalid il host name`() {
        Uri.parse("https://il-foo.srg.ch/media/ByUrn/urn:rts:video:123").toIlUrl()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toIlUrl throws an error with invalid urn`() {
        Uri.parse("${IlHost.PROD.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/").toIlUrl()
    }

    @Test
    fun `toIlUrl correctly filled`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.TV
        val ilLocation = IlLocation.WW
        val uri = Uri.parse("${host.baseHostUrl}/sam/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&forceLocation=$ilLocation")
        val expected = IlUrl(host = host, urn = urn, vector = vector, forceSAM = true, ilLocation = ilLocation)
        assertEquals(expected, uri.toIlUrl())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create IlUrl with invalid urn`() {
        IlUrl(host = IlHost.PROD, urn = "urn:invalid:1234", vector = Vector.MOBILE)
    }

    @Test
    fun `ILUrl create correct uri with default parameters`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val uri = Uri.parse("${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&onlyChapters=true")
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector)
        assertEquals(uri, ilUrl.uri)
    }

    @Test
    fun `ILUrl create correct uri with forceSAM`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val uri = Uri.parse("${host.baseHostUrl}/sam/integrationlayer/2.1/mediaComposition/byUrn/$urn?forceSAM=true&vector=$vector&onlyChapters=true")
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector, forceSAM = true)
        assertEquals(uri, ilUrl.uri)
    }

    @Test
    fun `ILUrl create correct uri with ilLocation`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val ilLocation = IlLocation.WW
        val uri = Uri.parse(
            "${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?forceLocation=$ilLocation&vector=$vector&onlyChapters=true"
        )
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector, ilLocation = ilLocation)
        assertEquals(uri, ilUrl.uri)
    }
}
