/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Default ktor HttpClient.
 */
object DefaultHttpClient {
    private val jsonSerializer = Json {
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
