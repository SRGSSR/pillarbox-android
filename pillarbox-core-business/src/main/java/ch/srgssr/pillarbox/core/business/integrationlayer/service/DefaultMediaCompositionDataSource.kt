/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendEncodedPathSegments
import java.net.URL

/**
 * Default media composition data source
 *
 * @property httpClient Ktor HttpClient to make requests.
 * @property baseUrl Base ur to make requests.
 * @property vector Vector to send with the requests. [Context.getVector()]
 */
class DefaultMediaCompositionDataSource(
    private val httpClient: HttpClient = DefaultHttpClient(),
    private val baseUrl: URL = IlHost.DEFAULT,
    private val vector: String = DEFAULT_VECTOR
) : MediaCompositionDataSource {

    override suspend fun getMediaCompositionByUrn(urn: String): Result<MediaComposition> {
        return Result.runCatching {
            httpClient.get(baseUrl) {
                url {
                    appendEncodedPathSegments("integrationlayer/2.1/mediaComposition/byUrn")
                    appendEncodedPathSegments(urn)
                    parameter("vector", vector)
                    parameter("onlyChapters", true)
                }
            }.body()
        }
    }

    companion object {
        private const val DEFAULT_VECTOR = Vector.MOBILE
    }
}
