/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class QoSSessionTest {
    @Test
    fun `contextConstructor provides correct default values`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.PHONE, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(sdk = [30])
    fun `contextConstructor provides correct default values (API 30)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("robolectric robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.PHONE, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("11", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "car")
    fun `contextConstructor provides correct default values for car`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.CAR, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "sw320dp")
    fun `contextConstructor provides correct default values for phone (sw320dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.PHONE, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun `contextConstructor provides correct default values for tablet (sw600dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.TABLET, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "sw720dp")
    fun `contextConstructor provides correct default values for tablet (sw720dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.TABLET, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "television")
    fun `contextConstructor provides correct default values for TV`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.TV, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    @Test
    @Config(qualifiers = "watch")
    fun `contextConstructor provides correct default values for watch`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.deviceId)
        assertEquals("unknown robolectric", qosSession.deviceModel)
        assertEquals(QoSSession.DeviceType.PHONE, qosSession.deviceType)
        assertEquals("android", qosSession.operatingSystemName)
        assertEquals("5.0.2", qosSession.operatingSystemVersion)
        assertEquals("ch.srgssr.pillarbox.player.test", qosSession.origin)
        assertEquals("pillarbox", qosSession.playerName)
        assertEquals("android", qosSession.playerPlatform)
        assertEquals("Local", qosSession.playerVersion)
        assertEquals(470, qosSession.screenHeight)
        assertEquals(320, qosSession.screenWidth)
        assertEquals(QoSSessionTimings.Empty, qosSession.timings)
    }

    private fun createQoSSession(): QoSSession {
        val context = ApplicationProvider.getApplicationContext<Context>()

        return QoSSession(
            context = context,
            mediaId = "urn:rts:video:12345",
            mediaSource = "https://il-stage.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/urn:rts:video:12345?vector=APPPLAY",
            timings = QoSSessionTimings.Empty,
        )
    }
}
