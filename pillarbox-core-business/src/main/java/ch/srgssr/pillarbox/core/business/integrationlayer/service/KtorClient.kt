/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.util.Log
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.appendEncodedPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val httpClient = HttpClient(OkHttp) {
        expectSuccess = true
        engine {
        }
        install(ContentNegotiation) {
            json(jsonSerializer)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Coucou", message)
                }
            }
            level = LogLevel.HEADERS
        }
    }

    val baseUrl = "https://il.prod.ch/il/"

    // baseUrl/mediaComposition/$urn
    suspend fun getMediaComposition(urn: String): MediaComposition {
        return httpClient.get(IlHost.PROD) {
            url {
                appendEncodedPathSegments("integrationlayer/2.1/mediaComposition/byUrn")
                appendEncodedPathSegments(urn)
            }
        }.body()
    }
}
