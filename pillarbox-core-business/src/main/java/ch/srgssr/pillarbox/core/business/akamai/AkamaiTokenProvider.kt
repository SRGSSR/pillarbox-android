/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import android.net.UrlQuerySanitizer
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider.Companion.TOKEN_SERVICE_URL
import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendEncodedPathSegments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Akamai token provider fetch and rewrite given Uri with a Token received from [TOKEN_SERVICE_URL].
 */
class AkamaiTokenProvider(private val httpClient: HttpClient = PillarboxHttpClient()) {

    /**
     * Request and append an Akamai token to [uri]
     *
     * @param uri protected by a token
     * @return tokenized [uri] or [uri] if it fails
     */
    suspend fun tokenizeUri(uri: Uri): Uri {
        val acl = getAcl(uri)
        val token = acl?.let {
            val tokenResult = getToken(it)
            tokenResult.getOrDefault(null)
        }
        return token?.let { appendTokenToUri(uri, it) } ?: uri
    }

    private suspend fun getToken(acl: String): Result<Token> {
        return runCatching {
            httpClient.get(TOKEN_SERVICE_URL) {
                url {
                    appendEncodedPathSegments("akahd/token")
                    parameter("acl", acl)
                }
            }.body<TokenResponse>().token
        }
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

    internal companion object {
        private const val TOKEN_SERVICE_URL = "https://tp.srgssr.ch/"

        internal fun getAcl(uri: Uri): String? {
            val path = uri.path
            if (path.isNullOrEmpty()) {
                return null
            }
            return path.substring(0, path.lastIndexOf("/") + 1) + "*"
        }

        internal fun appendTokenToUri(uri: Uri, token: Token): Uri {
            val sanitizer = UrlQuerySanitizer("u.rl/p?${token.authParams}")
            sanitizer.parseQuery(token.authParams)
            val builder = uri.buildUpon()
            for (key in sanitizer.parameterSet) {
                builder.appendQueryParameter(key, sanitizer.getValue(key))
            }
            return builder.build()
        }
    }
}
