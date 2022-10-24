/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.akamai

import android.net.Uri
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Akamai token provider fetch and rewrite given Uri with a Token received from [tokenService]
 *
 * @property tokenService
 */
class AkamaiTokenProvider(private val tokenService: Service) {
    constructor() : this(createService())

    /**
     * Append a token to [uri]
     *
     * @param uri
     * @return tokenized [uri] or [uri]
     */
    suspend fun tokenizeUri(uri: Uri): Uri {
        val acl = getAcl(uri)
        val token = acl?.let { tokenService.getToken(it).token }
        return token?.let { uri.buildUpon().encodedQuery(it.authParams).build() } ?: uri
    }

    private fun getAcl(uri: Uri): String? {
        val path = uri.path
        if (path == null || TextUtils.isEmpty(path)) {
            return null
        }
        return if (path.contains("/hls/playingLive/")) {
            "/hls/playingLive/*"
        } else {
            /* replace "master.m3u8" by "*" */
            path.substring(0, path.lastIndexOf("/") + 1) + "*"
        }
    }

    /**
     * Retrofit Service to get a [TokenResponse]
     */
    interface Service {

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
    data class Token(
        val country: String? = null,
        val ip: String? = null,
        val acl: String? = null,
        @SerializedName("authparams")
        val authParams: String? = null
    )

    /**
     * Token response
     *
     * @property token
     */
    data class TokenResponse(val token: Token)

    companion object {

        private const val TOKEN_URL = "https://tp.srgssr.ch/"

        private fun createService(): Service {
            return Retrofit.Builder()
                .baseUrl(TOKEN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Service::class.java)
        }
    }
}
