/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Type
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.utils.LocalMediaCompositionWithFallbackService
import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class HttpMediaCompositionTest {

    @Test
    fun `fallback service sends the platform and the DRM capabilities with real network call`() = runTest {
        val interceptor = RecordingInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "com.google.android.widevine",
            drmSecurityLevel = "L1",
        )
        val context: Context = ApplicationProvider.getApplicationContext()
        val service = LocalMediaCompositionWithFallbackService(context, HttpMediaCompositionService(client, deviceCapabilities))

        // The URN is not in the local media compositions, so the request falls back to HTTP and reaches the integration layer
        val result = service.fetchMediaComposition(REMOTE_MEDIA_COMPOSITION_URL.toUri())

        assertEquals(REMOTE_URN_TATAKI_1, result.getOrThrow().mediaComposition.chapterUrn)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends only the platform when DRM capabilities are partial`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val service = HttpMediaCompositionService(client) // Default Device capabilities

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow().mediaComposition)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(DeviceCapabilities.PLATFORM_ANDROID, requestedUrl.queryParameter("playerPlatform"))
        assertNull(requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends the platform and the DRM capabilities on Pixel 9`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "com.google.android.widevine",
            drmSecurityLevel = "L1",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow().mediaComposition)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends the platform and the DRM capabilities on SM-G925F (Android 7)`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "Google",
            drmSecurityLevel = "L1",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow().mediaComposition)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends the platform and the DRM capabilities on SM-F766B (flip phone)`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "com.google.android.widevine",
            drmSecurityLevel = "L1",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow().mediaComposition)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fallback service sends the platform and the DRM capabilities without network call`() = runTest {
        val interceptor = RecordingAbortingInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "com.google.android.widevine",
            drmSecurityLevel = "L1",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)

        // The URN is not in the local media compositions, so the request falls back to HTTP, where it is aborted by the interceptor
        val result = service.fetchMediaComposition(REMOTE_MEDIA_COMPOSITION_URL.toUri())

        assertTrue(result.isFailure)
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fallback service sends the platform and the DRM capabilities with real network call and good capabilities`() = runTest {
        val interceptor = RecordingInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "android",
            drmVendor = "com.google.android.widevine",
            drmSecurityLevel = "L1",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)
        val result = service.fetchMediaComposition(REMOTE_MEDIA_COMPOSITION_URL.toUri())

        assertEquals(
            result.getOrThrow().mediaComposition.mainChapter.listResource?.size,
            1
        ) // best resource retrieved by the server, no more than 1 item
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertEquals("${deviceCapabilities.drmVendor};${deviceCapabilities.drmSecurityLevel}", requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fallback service sends the platform and the DRM capabilities with real network call and no capabilities`() = runTest {
        val interceptor = RecordingInterceptor()
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val deviceCapabilities = DeviceCapabilities(
            platform = "",
        )
        val service = HttpMediaCompositionService(client, deviceCapabilities)

        // The URN is not in the local media compositions, so the request falls back to HTTP and reaches the integration layer
        val result = service.fetchMediaComposition(REMOTE_MEDIA_COMPOSITION_URL.toUri())

        assertEquals(
            result.getOrThrow().mediaComposition.mainChapter.listResource?.size,
            2
        ) // server cannot identify the best resource, more than 1 item
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(deviceCapabilities.platform, requestedUrl.queryParameter("playerPlatform"))
        assertNull(requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    /**
     * An [Interceptor] that records every requested URL, and lets the request reach the network.
     */
    private class RecordingInterceptor : Interceptor {
        /**
         * The URLs requested through this interceptor, in order.
         */
        val requestedUrls: List<HttpUrl>
            field = mutableListOf<HttpUrl>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requestedUrls.add(request.url)

            return chain.proceed(request)
        }
    }

    /**
     * An [Interceptor] that records every requested URL, and aborts the request before it reaches the network.
     */
    private class RecordingAbortingInterceptor : Interceptor {
        /**
         * The URLs requested through this interceptor, in order.
         */
        val requestedUrls: List<HttpUrl>
            field = mutableListOf<HttpUrl>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requestedUrls.add(request.url)

            throw IOException("Request aborted by the test")
        }
    }

    /**
     * An [Interceptor] that records every requested URL, and answers with [mediaComposition] without reaching the network.
     *
     * @param mediaComposition The [MediaComposition] returned for every request.
     */
    private class RecordingMediaCompositionInterceptor(
        private val mediaComposition: MediaComposition,
    ) : Interceptor {
        /**
         * The URLs requested through this interceptor, in order.
         */
        val requestedUrls: List<HttpUrl>
            field = mutableListOf<HttpUrl>()

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requestedUrls.add(request.url)

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(jsonSerializer.encodeToString(mediaComposition).toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private companion object {
        const val URN_HLS_RESOURCE = "urn:rts:video:resource_hls"
        const val MEDIA_COMPOSITION_URL = "https://il.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/$URN_HLS_RESOURCE"
        const val DUMMY_IMAGE_URL = "https://image.png"
        const val REMOTE_URN_TATAKI_1 = "urn:rts:video:13950405"
        const val REMOTE_MEDIA_COMPOSITION_URL = "https://il.srgssr.ch/integrationlayer/2.1/mediaComposition/byUrn/$REMOTE_URN_TATAKI_1"
        val MEDIA_COMPOSITION = createMediaComposition(
            urn = URN_HLS_RESOURCE,
            listResource = listOf(createResource(Resource.Type.HLS)),
        )

        fun createMediaComposition(urn: String, listResource: List<Resource>?): MediaComposition {
            return MediaComposition(
                urn,
                listOf(
                    Chapter(
                        urn = urn,
                        title = urn,
                        listResource = listResource,
                        imageUrl = DUMMY_IMAGE_URL,
                        mediaType = MediaType.VIDEO,
                        type = Type.EPISODE,
                    )
                )
            )
        }

        fun createResource(type: Resource.Type): Resource {
            return Resource(url = "", type = type)
        }
    }
}
