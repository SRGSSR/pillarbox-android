/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import androidx.annotation.VisibleForTesting
import ch.srgssr.pillarbox.player.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * Provide a Ktor [HttpClient] instance tailored for Pillarbox's needs.
 */
object PillarboxHttpClient {
    /**
     * The [Json] serializer used by this [HttpClient].
     */
    @OptIn(ExperimentalSerializationApi::class)
    @VisibleForTesting
    val jsonSerializer = Json {
        classDiscriminatorMode = ClassDiscriminatorMode.NONE
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            engine {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        val logLevel = if (BuildConfig.DEBUG) Level.BODY else Level.NONE

                        setLevel(logLevel)
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
     * Returns the [HttpClient] tailored for Pillarbox's needs.
     *
     * @return A [HttpClient] instance.
     */
    operator fun invoke(): HttpClient {
        return httpClient
    }
}
