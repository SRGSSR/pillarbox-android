/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.network

import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.network.jsonSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

/**
 * Provides a pre-configured Ktor [HttpClient] instance tailored for Pillarbox's specific needs.
 */
object PillarboxHttpClient {
    private val httpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            engine {
                preconfigured = PillarboxOkHttp()
            }

            install(HttpCache)
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
        }
    }

    /**
     * Provides access to the underlying [HttpClient] instance configured for Pillarbox.
     *
     * @return The [HttpClient] instance used by Pillarbox.
     */
    operator fun invoke(): HttpClient {
        return httpClient
    }
}
