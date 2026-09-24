/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaType
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Type
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class HttpMediaCompositionTest {

    @Test
    fun `fetchMediaComposition sends only the platform when DRM capabilities are unknown`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val service = HttpMediaCompositionService(client, DeviceCapabilities())

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow())
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals(DeviceCapabilities.PLATFORM_ANDROID, requestedUrl.queryParameter("playerPlatform"))
        assertNull(requestedUrl.queryParameter("drmPlayerCapabilities"))
    }

    @Test
    fun `fetchMediaComposition sends only the platform when DRM capabilities are partial`() = runTest {
        val interceptor = RecordingMediaCompositionInterceptor(MEDIA_COMPOSITION)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val service = HttpMediaCompositionService(client, DeviceCapabilities(drmVendor = "Google"))

        val result = service.fetchMediaComposition(MEDIA_COMPOSITION_URL.toUri())

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow())
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

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow())
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals("android", requestedUrl.queryParameter("playerPlatform"))
        assertEquals("com.google.android.widevine;L1", requestedUrl.queryParameter("drmPlayerCapabilities"))
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

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow())
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals("android", requestedUrl.queryParameter("playerPlatform"))
        assertEquals("Google;L1", requestedUrl.queryParameter("drmPlayerCapabilities"))
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

        assertEquals(MEDIA_COMPOSITION, result.getOrThrow())
        val requestedUrl = interceptor.requestedUrls.single()
        assertEquals("android", requestedUrl.queryParameter("playerPlatform"))
        assertEquals("com.google.android.widevine;L1", requestedUrl.queryParameter("drmPlayerCapabilities"))
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
