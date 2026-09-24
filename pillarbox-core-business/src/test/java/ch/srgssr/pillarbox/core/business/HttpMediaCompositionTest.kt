/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Segment
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeInterval
import ch.srgssr.pillarbox.core.business.integrationlayer.data.TimeIntervalType
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Type
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.source.SegmentAdapter
import ch.srgssr.pillarbox.core.business.source.TimeIntervalAdapter
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class HttpMediaCompositionTest {

    @Test
    fun `fetchMediaComposition sends the platform and the DRM capabilities`() = runTest {
        val requestedUrls = mutableListOf<HttpUrl>()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "Google",
            drmSecurityLevel = "L1",
        )

        val assetLoader = SRGAssetLoader(ApplicationProvider.getApplicationContext()) {
            mediaCompositionService(HttpMediaCompositionService(createOkHttpClient(requestedUrls), deviceCapabilities))
        }

        assetLoader.loadAsset(SRGMediaItem(SRGAssetLoaderTest.DummyMediaCompositionProvider.URN_HLS_RESOURCE))

        val requestedUrl = requestedUrls.single()
        assertEquals("android", requestedUrl.queryParameter("playerPlatform"))
        assertEquals("Google;L1", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends only the platform when DRM capabilities are unknown`() = runTest {
        val requestedUrls = mutableListOf<HttpUrl>()
        val assetLoader = SRGAssetLoader(ApplicationProvider.getApplicationContext()) {
            mediaCompositionService(HttpMediaCompositionService(createOkHttpClient(requestedUrls), DeviceCapabilities()))
        }

        assetLoader.loadAsset(SRGMediaItem(SRGAssetLoaderTest.DummyMediaCompositionProvider.URN_HLS_RESOURCE))

        val requestedUrl = requestedUrls.single()
        assertEquals(DeviceCapabilities.PLATFORM_ANDROID, requestedUrl.queryParameter("playerPlatform"))
        assertNull(requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends only the platform when DRM capabilities are partial`() = runTest {
        val requestedUrls = mutableListOf<HttpUrl>()
        val client = createOkHttpClient(requestedUrls)
        val assetLoader = SRGAssetLoader(ApplicationProvider.getApplicationContext()) {
            mediaCompositionService(HttpMediaCompositionService(client, DeviceCapabilities(drmVendor = "Google")))
        }

        assetLoader.loadAsset(SRGMediaItem(SRGAssetLoaderTest.DummyMediaCompositionProvider.URN_HLS_RESOURCE))

        val requestedUrl = requestedUrls.single()
        assertEquals(DeviceCapabilities.PLATFORM_ANDROID, requestedUrl.queryParameter("playerPlatform"))
        assertNull(requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    /**
     * Creates an [OkHttpClient] that records every requested URL in [requestedUrls], and answers with a playable [MediaComposition] without
     * reaching the network.
     */
    private fun createOkHttpClient(requestedUrls: MutableList<HttpUrl>): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                requestedUrls.add(request.url)

                val mediaComposition = SRGAssetLoaderTest.DummyMediaCompositionProvider.createMediaComposition(
                    urn = SRGAssetLoaderTest.DummyMediaCompositionProvider.URN_HLS_RESOURCE,
                    listResource = listOf(SRGAssetLoaderTest.DummyMediaCompositionProvider.createResource(Resource.Type.HLS)),
                )

                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(jsonSerializer.encodeToString(mediaComposition).toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
    }
}
