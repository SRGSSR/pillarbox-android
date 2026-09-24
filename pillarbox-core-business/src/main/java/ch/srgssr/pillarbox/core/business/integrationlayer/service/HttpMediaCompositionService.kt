/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaCompositionResponse
import ch.srgssr.pillarbox.player.network.HttpResultException
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * A service for fetching a [MediaComposition] over HTTP.
 *
 * @param okHttpClient The OkHttp client instance used for making HTTP requests.
 * @param deviceCapabilities Describes the playback capabilities of the device, so that the integration layer only returns resources that the
 * device is actually able to play.
 *
 */
class HttpMediaCompositionService(
    private val okHttpClient: OkHttpClient = PillarboxOkHttp(),
    private val deviceCapabilities: DeviceCapabilities = DeviceCapabilities.device
) : MediaCompositionService {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaCompositionResponse> {
        return runCatching {
            okHttpClient.newCall(
                Request.Builder()
                    .url(withPlayerCapabilities(uri).toString())
                    .build()
            )
                .execute()
        }.mapCatching { rawResponse ->
            rawResponse.use { response ->
                if (!response.isSuccessful) {
                    throw HttpResultException(response.code, response.message)
                }
                val bodyStream = checkNotNull(response.body).byteStream()
                val headers = response.headers.toMultimap()
                val mediaCompositionParsed = jsonSerializer.decodeFromStream<MediaComposition>(bodyStream)
                MediaCompositionResponse(mediaCompositionParsed, headers)
            }
        }
    }

    /**
     * Appends [deviceCapabilities] to [uri] as integration layer query parameters.
     */
    internal fun withPlayerCapabilities(uri: Uri): Uri {
        val drmVendor = deviceCapabilities.drmVendor
        val drmSecurityLevel = deviceCapabilities.drmSecurityLevel

        return uri.buildUpon().apply {
            appendQueryParameter(PARAM_PLAYER_PLATFORM, deviceCapabilities.platform)

            if (drmVendor != null && drmSecurityLevel != null) {
                appendQueryParameter(PARAM_DRM_PLAYER_CAPABILITIES, "$drmVendor;$drmSecurityLevel")
            }
        }.build()
    }

    private companion object {
        const val PARAM_PLAYER_PLATFORM = "playerPlatform"
        const val PARAM_DRM_PLAYER_CAPABILITIES = "drmPlayerCapabilities"
    }
}
