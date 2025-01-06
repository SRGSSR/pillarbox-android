/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import ch.srgssr.pillarbox.player.network.RequestSender.send
import ch.srgssr.pillarbox.player.network.RequestSender.toJsonRequestBody
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RequestSenderTest {
    private lateinit var buffer: Buffer

    @BeforeTest
    fun setUp() {
        buffer = Buffer()
    }

    @AfterTest
    fun tearDown() {
        buffer.close()

        clearAllMocks()
    }

    @Test
    fun `to JSON request body, Int`() {
        validateRequestBodyConversion(42)
    }

    @Test
    fun `to JSON request body, String`() {
        validateRequestBodyConversion("Hello, World!")
    }

    @Test
    fun `to JSON request body, serializable Model`() {
        validateRequestBodyConversion(SerializableModel(name = "Bruce Wayne", place = "Gotham City"))
    }

    @Test(expected = SerializationException::class)
    fun `to JSON request body, non serializable Model`() {
        NonSerializableModel(name = "The Joker", place = "Arkham City").toJsonRequestBody()
    }

    @Test
    fun `send request, 20x`() {
        val okHttpClient = createFakeOkHttpClient(statusCode = 200, contentBody = "Hello")
        val result = Request.Builder()
            .url("https://server.com/")
            .build()
            .send<String>(okHttpClient)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `send request, 40x`() {
        val okHttpClient = createFakeOkHttpClient(statusCode = 404, contentBody = "Not found")
        val result = Request.Builder()
            .url("https://server.com/")
            .build()
            .send<Unit>(okHttpClient)
        val exception = result.exceptionOrNull()

        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertNotNull(exception)
        assertIs<HttpResultException>(exception)
    }

    private inline fun <reified T> validateRequestBodyConversion(data: T) {
        val requestBody = data.toJsonRequestBody()
        requestBody.writeTo(buffer)

        assertEquals(RequestSender.MIME_TYPE_JSON, requestBody.contentType())
        assertEquals(jsonSerializer.encodeToString(data), buffer.readUtf8())
    }

    private companion object {
        private fun createFakeOkHttpClient(
            statusCode: Int,
            contentBody: String,
        ): OkHttpClient {
            return mockk<OkHttpClient> {
                every { newCall(any()) } returns mockk {
                    every { execute() } returns mockk {
                        every { isSuccessful } returns (statusCode in 200..299)
                        every { body } returns contentBody.toResponseBody(RequestSender.MIME_TYPE_JSON)
                        every { message } returns contentBody
                        every { code } returns statusCode
                        every { close() } returns Unit
                    }
                }
            }
        }
    }

    @Serializable
    private data class SerializableModel(
        val name: String,
        val place: String,
    )

    private data class NonSerializableModel(
        val name: String,
        val place: String,
    )
}
