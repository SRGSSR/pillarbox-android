/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendEncodedPathSegments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Akamai token provider fetch and rewrite given Uri with a Token received from [tokenService]
 *
 */
class AkamaiTokenProvider(private val httpClient: HttpClient = DefaultHttpClient()) {

    /**
     * Request and append a Akamai token to [uri]
     *
     * @param uri protected by a token
     * @return tokenized [uri] or [uri] if it fail
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    suspend fun tokenizeUri(uri: Uri): Uri {
        val acl = getAcl(uri)
        val token = acl?.let {
            try {
                getToken(it).token
            } catch (e: Exception) {
                null
            }
        }
        return token?.let { uri.buildUpon().encodedQuery(it.authParams).build() } ?: uri
    }

    private suspend fun getToken(acl: String): TokenResponse {
        return httpClient.get(TOKEN_SERVICE_URL) {
            url {
                appendEncodedPathSegments("akahd/token")
                parameter("acl", acl)
            }
        }.body()
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
        private const val TOKEN_SERVICE_URL = "https://tp.srgssr.ch/"

        internal fun getAcl(uri: Uri): String? {
            val path = uri.path
            if (path.isNullOrEmpty()) {
                return null
            }
            return path.substring(0, path.lastIndexOf("/") + 1) + "*"
        }
    }
}
