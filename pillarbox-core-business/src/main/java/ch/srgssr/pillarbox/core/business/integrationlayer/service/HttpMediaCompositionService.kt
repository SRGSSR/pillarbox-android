/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL

/**
 * A service for fetching a [MediaComposition] over HTTP.
 *
 * @param httpClient The Ktor [HttpClient] instance used for making HTTP requests.
 */
class HttpMediaCompositionService(
    private val httpClient: HttpClient = PillarboxHttpClient(),
) : MediaCompositionService {

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        return runCatching {
            httpClient.get(URL(uri.toString()))
                .body()
        }
    }
}
