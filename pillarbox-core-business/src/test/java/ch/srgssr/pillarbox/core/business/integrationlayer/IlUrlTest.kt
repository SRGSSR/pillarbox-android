/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl.Companion.toIlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        val uri = Uri.parse(
            "${host.baseHostUrl}/sam/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&forceLocation=$ilLocation"
        )
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
        val uri = Uri.parse(
            "${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&onlyChapters=true" +
                "&playerPlatform=android"
        )
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector)
        assertEquals(uri, ilUrl.uri)
    }

    @Test
    fun `ILUrl create correct uri with forceSAM`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val uri = Uri.parse(
            "${host.baseHostUrl}/sam/integrationlayer/2.1/mediaComposition/byUrn/$urn?forceSAM=true&vector=$vector" +
                "&onlyChapters=true" +
                "&playerPlatform=android"
        )
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
            "${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?forceLocation=$ilLocation&vector=$vector" +
                "&onlyChapters=true" +
                "&playerPlatform=android"
        )
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector, ilLocation = ilLocation)
        assertEquals(uri, ilUrl.uri)
    }

    @Test
    fun `ILUrl uri contains the platform and the DRM capabilities`() {
        val host = IlHost.PROD
        val deviceCapabilities = DeviceCapabilities(drmSecurityLevel = "L3")
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val ilUrl = IlUrl(host = IlHost.PROD, urn = "urn:rts:video:1234", vector = vector, deviceCapabilities = deviceCapabilities)
        val uri = Uri.parse(
            "${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector" +
                "&onlyChapters=true" +
                "&playerPlatform=android" + "&drmPlayerCapabilities=com.widevine.alpha%3BL3"
        )
        assertEquals("android", ilUrl.uri.getQueryParameter("playerPlatform"))
        assertEquals("com.widevine.alpha;L3", ilUrl.uri.getQueryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `ILUrl uri omits the DRM capabilities when the security level is unknown`() {
        val deviceCapabilities = DeviceCapabilities()
        val ilUrl = IlUrl(host = IlHost.PROD, urn = "urn:rts:video:1234", vector = Vector.MOBILE, deviceCapabilities = deviceCapabilities)

        assertEquals(DeviceCapabilities.PLATFORM_ANDROID, ilUrl.uri.getQueryParameter("playerPlatform"))
        assertNull(ilUrl.uri.getQueryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `toIlUrl ignores the capabilities query parameters`() {
        val host = IlHost.PROD
        val urn = "urn:rts:video:1234"
        val vector = Vector.MOBILE
        val uri = Uri.parse(
            "${host.baseHostUrl}/integrationlayer/2.1/mediaComposition/byUrn/$urn?vector=$vector&onlyChapters=true" +
                "&playerPlatform=android&drmPlayerCapabilities=com.widevine.alpha%3BL1"
        )
        val expected = IlUrl(host = host, urn = urn, vector = vector)
        assertEquals(expected, uri.toIlUrl())
    }
}
