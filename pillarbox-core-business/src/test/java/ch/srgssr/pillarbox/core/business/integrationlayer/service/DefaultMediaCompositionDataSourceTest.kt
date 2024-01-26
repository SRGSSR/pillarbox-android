/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultMediaCompositionDataSourceTest {
    @Test
    fun `get media composition by urn`() = runTest {
        val vector = Vector.MOBILE
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                        "chapterUrn": "$URN",
                        "chapterList": []
                    }
                """.trimIndent(),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val mediaCompositionDataSource = DefaultMediaCompositionDataSource(
            httpClient = createHttpClient(mockEngine),
            baseUrl = ilHost,
            vector = vector,
        )

        val mediaCompositionResult = mediaCompositionDataSource.getMediaCompositionByUrn(URN)
        val requestUrl = mockEngine.requestHistory.singleOrNull()?.url?.toString()

        assertTrue(mediaCompositionResult.isSuccess)
        assertEquals("${ilHost}integrationlayer/2.1/mediaComposition/byUrn/$URN?vector=$vector&onlyChapters=true", requestUrl)

        val mediaComposition = mediaCompositionResult.getOrThrow()
        assertEquals(URN, mediaComposition.chapterUrn)
        assertTrue(mediaComposition.listChapter.isEmpty())
    }

    @Test
    fun `get media composition by urn, when request fails`() = runTest {
        val vector = Vector.TV
        val mockEngine = MockEngine {
            respondBadRequest()
        }
        val mediaCompositionDataSource = DefaultMediaCompositionDataSource(
            httpClient = createHttpClient(mockEngine),
            baseUrl = ilHost,
            vector = vector,
        )

        val mediaCompositionResult = mediaCompositionDataSource.getMediaCompositionByUrn(URN)
        val requestUrl = mockEngine.requestHistory.singleOrNull()?.url?.toString()

        assertTrue(mediaCompositionResult.isFailure)
        assertEquals("${ilHost}integrationlayer/2.1/mediaComposition/byUrn/$URN?vector=$vector&onlyChapters=true", requestUrl)
    }

    private companion object {
        private val ilHost = IlHost.TEST
        private const val URN = "urn:rts:video:123345"

        private fun createHttpClient(engine: HttpClientEngine): HttpClient {
            return HttpClient(engine) {
                expectSuccess = true

                install(ContentNegotiation) {
                    json(DefaultHttpClient.jsonSerializer)
                }
            }
        }
    }
}
