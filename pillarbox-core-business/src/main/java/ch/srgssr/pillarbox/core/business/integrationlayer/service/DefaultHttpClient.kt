/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Default Ktor [HttpClient].
 *
 * This class is deprecated in favor of [PillarboxHttpClient].
 * The latter provides a similar setup as this class, with the following differences:
 * - `classDiscriminatorMode` set to [ClassDiscriminatorMode.NONE]: don't include class name in the JSON output for polymorphic classes.
 * - `explicitNulls` set to `false`: don't include `null` fields in the JSON output.
 * - Logging is set to `BODY` in debug, and `NONE otherwise.
 */
@Deprecated(
    message = "Use `PillarboxHttpClient` instead.",
    replaceWith = ReplaceWith("PillarboxHttpClient", imports = ["ch.srgssr.pillarbox.player.network.PillarboxHttpClient"]),
)
object DefaultHttpClient {
    internal val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true
            engine {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.HEADERS)
                    }
                )
            }
            install(HttpCache)
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
        }
    }

    /**
     * Invoke
     *
     * @return [httpClient]
     */
    operator fun invoke(): HttpClient {
        return httpClient
    }
}
