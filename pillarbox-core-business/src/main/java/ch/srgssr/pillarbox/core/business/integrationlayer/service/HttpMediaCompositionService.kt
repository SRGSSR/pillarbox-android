/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL

/**
 * Http MediaCompositionService.
 *
 * Fetch MediaComposition threw an HttpClient.
 *
 * @param httpClient Ktor HttpClient to make requests.
 */
class HttpMediaCompositionService(
    private val httpClient: HttpClient = DefaultHttpClient(),
) : MediaCompositionService {

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        return runCatching {
            httpClient.get(URL(uri.toString()))
                .body()
        }
    }
}
