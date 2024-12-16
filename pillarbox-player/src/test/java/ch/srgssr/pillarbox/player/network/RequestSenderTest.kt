/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import ch.srgssr.pillarbox.player.network.RequestSender.send
import ch.srgssr.pillarbox.player.network.RequestSender.toJsonRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import okhttp3.Request
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
        val result = Request.Builder()
            .url("https://httpbin.org/get")
            .build()
            .send<Unit>()

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `send request, 40x`() {
        val result = Request.Builder()
            .url("https://httpbin.org/status/404")
            .build()
            .send<Unit>()
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
