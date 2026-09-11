/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaCompositionResponse
import ch.srgssr.pillarbox.player.network.HttpResultException
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.network.RequestSender.sendRaw
import ch.srgssr.pillarbox.player.network.jsonSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * A service for fetching a [MediaComposition] over HTTP.
 *
 * @param okHttpClient The OkHttp client instance used for making HTTP requests.
 */
class HttpMediaCompositionService(
    private val okHttpClient: OkHttpClient = PillarboxOkHttp(),
) : MediaCompositionService {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaCompositionResponse> {
        return Request.Builder()
            .url(uri.toString())
            .build()
            .sendRaw(okHttpClient)
            .mapCatching { rawResponse ->
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
}
