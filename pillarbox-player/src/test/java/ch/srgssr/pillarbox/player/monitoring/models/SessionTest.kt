/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class SessionTest {
    @Test
    fun `contextConstructor provides correct default values`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(sdk = [30])
    fun `contextConstructor provides correct default values (API 30)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("robolectric robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("11", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "car")
    fun `contextConstructor provides correct default values for car`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.CAR, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "desk")
    fun `contextConstructor provides correct default values for desktop`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.DESKTOP, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "sw320dp")
    fun `contextConstructor provides correct default values for phone (sw320dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun `contextConstructor provides correct default values for tablet (sw600dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.TABLET, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "sw720dp")
    fun `contextConstructor provides correct default values for tablet (sw720dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.TABLET, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "television")
    fun `contextConstructor provides correct default values for TV`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(Session.Device.Type.TV, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    @Test
    @Config(qualifiers = "watch")
    fun `contextConstructor provides correct default values for watch`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertNull(qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(Timings.QoE(), qosSession.qoeTimings)
        assertEquals(Timings.QoS(), qosSession.qosTimings)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
    }

    private fun createQoSSession(): Session {
        val context = ApplicationProvider.getApplicationContext<Context>()

        return Session(
            context = context,
            media = Session.Media(
                assetUrl = ASSET_URL,
                id = MEDIA_ID,
                metadataUrl = METADATA_URL,
                origin = ORIGIN,
            ),
            qoeTimings = Timings.QoE(),
            qosTimings = Timings.QoS(),
        )
    }

    private companion object {
        private const val ASSET_URL = "https://rts-vod-amd.akamaized.net/ww/12345/3037738d-fe91-32e3-93f2-4dbb62a0f9bd/master.m3u8"
        private const val MEDIA_ID = "urn:rts:video:12345"
        private const val METADATA_URL = "https://il-stage.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/urn:rts:video:12345?vector=APPPLAY"
        private const val OPERATING_SYSTEM_NAME = "Android"
        private const val ORIGIN = "ch.srgssr.pillarbox.player.test"
        private const val PLAYER_NAME = "Pillarbox"
        private const val PLAYER_PLATFORM = "Android"
        private const val PLAYER_VERSION = "Local"
        private const val SCREEN_HEIGHT = 470
        private const val SCREEN_WIDTH = 320
    }
}
