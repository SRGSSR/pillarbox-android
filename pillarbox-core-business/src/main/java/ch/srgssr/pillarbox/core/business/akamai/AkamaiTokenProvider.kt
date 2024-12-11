/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import android.net.UrlQuerySanitizer
import ch.srgssr.pillarbox.player.network.RequestSender.send
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Request

/**
 * The [AkamaiTokenProvider] is responsible for fetching an Akamai token from `TOKEN_SERVICE_URL` and appending it to URIs.
 */
class AkamaiTokenProvider {

    /**
     * Requests and appends an Akamai token to the provided URI.
     *
     * If the retrieval of the token fails, the original [uri] is returned.
     *
     * @param uri The URI to be tokenized.
     * @return The tokenized [Uri] if successful, otherwise the original [uri].
     */
    fun tokenizeUri(uri: Uri): Uri {
        val acl = getAcl(uri)
        val token = acl?.let {
            val tokenResult = getToken(it)
            tokenResult.getOrDefault(null)
        }
        return token?.let { appendTokenToUri(uri, it) } ?: uri
    }

    private fun getToken(acl: String): Result<Token> {
        return runCatching {
            val request = Request.Builder()
                .url(TOKEN_SERVICE_URL.format(acl))
                .build()

            checkNotNull(request.send<TokenResponse>()).token
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
        private const val TOKEN_SERVICE_URL = "https://tp.srgssr.ch/akahd/token?acl=%s"

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
