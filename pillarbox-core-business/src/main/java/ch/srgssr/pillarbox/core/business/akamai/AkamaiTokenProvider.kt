/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Akamai token provider fetch and rewrite given Uri with a Token received from [tokenService]
 *
 * @property tokenService
 */
class AkamaiTokenProvider private constructor(private val tokenService: Service) {
    constructor() : this(createService())

    /**
     * Request and append a Akamai token to [uri]
     *
     * @param uri protected by a token
     * @return tokenized [uri] or [uri] if it fail
     */
    suspend fun tokenizeUri(uri: Uri): Uri {
        val acl = getAcl(uri)
        val token = acl?.let { tokenService.getToken(it).token }
        return token?.let { uri.buildUpon().encodedQuery(it.authParams).build() } ?: uri
    }

    /**
     * Retrofit Service to get a [TokenResponse]
     */
    private interface Service {

        /**
         * Get token from an Uri
         *
         * @param acl of an Uri
         */
        @GET("akahd/token")
        suspend fun getToken(@Query("acl") acl: String): TokenResponse
    }

    /**
     * Token received from Token service
     *
     * @property country
     * @property ip
     * @property acl
     * @property authParams
     */
    @Serializable
    internal data class Token(
        val country: String? = null,
        val ip: String? = null,
        val acl: String? = null,
        @SerialName("authparams")
        val authParams: String? = null
    )

    /**
     * Token response
     *
     * @property token
     */
    @Serializable
    internal data class TokenResponse(val token: Token)

    companion object {
        private val json: Json = Json { ignoreUnknownKeys = true }
        private const val TOKEN_SERVICE_URL = "https://tp.srgssr.ch/"

        internal fun getAcl(uri: Uri): String? {
            val path = uri.path
            if (path.isNullOrEmpty()) {
                return null
            }
            return path.substring(0, path.lastIndexOf("/") + 1) + "*"
        }

        private fun createService(): Service {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(TOKEN_SERVICE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(Service::class.java)
        }
    }
}
