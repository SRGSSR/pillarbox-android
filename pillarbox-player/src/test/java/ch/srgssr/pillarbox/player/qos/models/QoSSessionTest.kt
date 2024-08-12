/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

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

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(sdk = [30])
    fun `contextConstructor provides correct default values (API 30)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("robolectric robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("11", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "car")
    fun `contextConstructor provides correct default values for car`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.CAR, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "desk")
    fun `contextConstructor provides correct default values for desktop`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.DESKTOP, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "sw320dp")
    fun `contextConstructor provides correct default values for phone (sw320dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.PHONE, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun `contextConstructor provides correct default values for tablet (sw600dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.TABLET, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "sw720dp")
    fun `contextConstructor provides correct default values for tablet (sw720dp)`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.TABLET, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "television")
    fun `contextConstructor provides correct default values for TV`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.TV, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    @Test
    @Config(qualifiers = "watch")
    fun `contextConstructor provides correct default values for watch`() {
        val qosSession = createQoSSession()

        assertEquals("", qosSession.device.id)
        assertEquals("unknown robolectric", qosSession.device.model)
        assertEquals(QoSDevice.DeviceType.UNKNOWN, qosSession.device.type)
        assertEquals(ASSET_URL, qosSession.media.assetUrl)
        assertEquals(MEDIA_ID, qosSession.media.id)
        assertEquals(METADATA_URL, qosSession.media.metadataUrl)
        assertEquals(ORIGIN, qosSession.media.origin)
        assertEquals(OPERATING_SYSTEM_NAME, qosSession.operatingSystem.name)
        assertEquals("5.0.2", qosSession.operatingSystem.version)
        assertEquals(PLAYER_NAME, qosSession.player.name)
        assertEquals(PLAYER_PLATFORM, qosSession.player.platform)
        assertEquals(PLAYER_VERSION, qosSession.player.version)
        assertEquals(SCREEN_HEIGHT, qosSession.screen.height)
        assertEquals(SCREEN_WIDTH, qosSession.screen.width)
        assertEquals(QoSSessionTimings.Empty, qosSession.timeMetrics)
    }

    private fun createQoSSession(): QoSSession {
        val context = ApplicationProvider.getApplicationContext<Context>()

        return QoSSession(
            context = context,
            media = QoSMedia(
                assetUrl = ASSET_URL,
                id = MEDIA_ID,
                metadataUrl = METADATA_URL,
                origin = ORIGIN,
            ),
            timeMetrics = QoSSessionTimings.Empty,
        )
    }

    private companion object {
        private const val ASSET_URL = "https://rts-vod-amd.akamaized.net/ww/12345/3037738d-fe91-32e3-93f2-4dbb62a0f9bd/master.m3u8"
        private const val MEDIA_ID = "urn:rts:video:12345"
        private const val METADATA_URL = "https://il-stage.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/urn:rts:video:12345?vector=APPPLAY"
        private const val OPERATING_SYSTEM_NAME = "android"
        private const val ORIGIN = "ch.srgssr.pillarbox.player.test"
        private const val PLAYER_NAME = "pillarbox"
        private const val PLAYER_PLATFORM = "android"
        private const val PLAYER_VERSION = "Local"
        private const val SCREEN_HEIGHT = 470
        private const val SCREEN_WIDTH = 320
    }
}