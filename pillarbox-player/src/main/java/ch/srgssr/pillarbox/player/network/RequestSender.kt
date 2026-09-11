/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import androidx.annotation.RestrictTo
import ch.srgssr.pillarbox.player.network.RequestSender.send
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * A helper object responsible for sending HTTP requests using OkHttp and handling JSON serialization.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object RequestSender {
    /**
     * Represents the MIME type for JSON data.
     */
    val MIME_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Converts an object of type [T] to a [RequestBody] with JSON content type.
     *
     * @receiver The object to be converted to a [RequestBody].
     * @return A [RequestBody] containing the JSON representation of the receiver object.
     */
    inline fun <reified T> T.toJsonRequestBody(): RequestBody {
        return jsonSerializer.encodeToString(this)
            .toRequestBody(MIME_TYPE_JSON)
    }

    /**
     * Sends the current request and attempts to decode the response body into an object of type [T].
     *
     * @param T The type of object to decode the response body into.
     * @param okHttpClient The OkHttp client used to make requests to the token service. Defaults to a [PillarboxOkHttp] instance.
     *
     * @return A [Result] object containing either the successfully decoded object of type [T] or a [Throwable] representing the error that occurred.
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> Request.send(okHttpClient: OkHttpClient = PillarboxOkHttp()): Result<T> {
        return runCatching {
            okHttpClient.newCall(this)
                .execute()
                .use { response ->
                    if (response.isSuccessful) {
                        val bodyStream = checkNotNull(response.body).byteStream()

                        jsonSerializer.decodeFromStream(bodyStream)
                    } else {
                        throw HttpResultException(response.code, response.message)
                    }
                }
        }
    }

    /**
     * Sends the current request and returns the full [Response], without inspecting its status code nor decoding its body.
     *
     * Unlike [send], the returned [Response] is still open: its body has not been consumed, and unsuccessful status codes are not turned into a
     * [HttpResultException]. The caller therefore owns the response and **must** close it, ideally with [use][okhttp3.Response.use]:
     *
     * ```kotlin
     * request.sendRaw().onSuccess { response ->
     *     response.use {
     *         // Do something with the response
     *     }
     * }
     * ```
     *
     * @param okHttpClient The OkHttp client used to make requests to the token service. Defaults to a [PillarboxOkHttp] instance.
     *
     * @return A [Result] object containing either the full [Response], which the caller has to close, or a [Throwable] representing the error that
     * occurred while performing the request.
     */
    fun Request.sendRaw(okHttpClient: OkHttpClient = PillarboxOkHttp()): Result<Response> {
        return runCatching {
            okHttpClient.newCall(this)
                .execute()
        }
    }
}
