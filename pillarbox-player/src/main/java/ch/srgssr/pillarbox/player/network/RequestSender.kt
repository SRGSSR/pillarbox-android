/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import androidx.annotation.RestrictTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * A helper object responsible for sending HTTP requests using OkHttp and handling JSON serialization.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object RequestSender {
    private val MIME_TYPE_JSON = "application/json".toMediaType()

    /**
     * The OkHttp client used for network requests.
     */
    val okHttpClient = PillarboxOkHttp()

    /**
     * Converts any object to a [RequestBody] with JSON content type.
     *
     * @return A [RequestBody] containing the JSON representation of the object.
     */
    fun Any.toJsonRequestBody(): RequestBody {
        return jsonSerializer.encodeToString(this)
            .toRequestBody(MIME_TYPE_JSON)
    }

    /**
     * Sends the current request and decodes the response body into an object of type [T].
     *
     * @param T The type of object to decode the response body into.
     * @return An object of type [T] decoded from the response body, or `null` if the request fails or the body cannot be decoded.
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> Request.send(): T? {
        return okHttpClient.newCall(this)
            .execute()
            .use { response ->
                response.body
                    ?.byteStream()
                    ?.let { jsonSerializer.decodeFromStream<T>(it) }
            }
    }
}
