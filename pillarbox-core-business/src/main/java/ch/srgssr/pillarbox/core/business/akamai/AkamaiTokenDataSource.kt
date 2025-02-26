/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import ch.srgssr.pillarbox.player.utils.DebugLogger

/**
 * A [DataSource] that injects an Akamai token into URLs containing the query parameter `withToken=true`.
 */
class AkamaiTokenDataSource private constructor(
    private val tokenProvider: AkamaiTokenProvider,
    private val dataSource: DataSource
) : DataSource by dataSource {

    override fun open(dataSpec: DataSpec): Long {
        var outputUri = dataSpec.uri
        if (hasNeedAkamaiToken(outputUri)) {
            DebugLogger.debug("Akamai", "open ${dataSpec.uri}")
            val cleanUri = removeTokenQueryParameter(outputUri)
            outputUri = tokenProvider.tokenizeUri(cleanUri)
            return dataSource.open(dataSpec.buildUpon().setUri(outputUri).build())
        }
        return dataSource.open(dataSpec)
    }

    /**
     * A factory for creating instances of [AkamaiTokenDataSource].
     *
     * @param tokenProvider The [AkamaiTokenProvider] for generating tokens.
     * @param defaultDataSourceFactory The underlying [DataSource] to handle the request, by default [DefaultHttpDataSource].
     */
    class Factory(
        private val tokenProvider: AkamaiTokenProvider = AkamaiTokenProvider(),
        private val defaultDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return AkamaiTokenDataSource(tokenProvider, defaultDataSourceFactory.createDataSource())
        }
    }

    companion object {
        /**
         * Token Query Param to add to trigger token request
         */
        private const val TOKEN_QUERY_PARAM = "withToken"

        /**
         * Appends a query parameter to the provided URI indicating the need for an Akamai token.
         *
         * @param uri The original URI to which the query parameter should be added.
         * @return A new URI with the added token query parameter.
         */
        fun appendTokenQueryToUri(uri: Uri): Uri {
            return uri.buildUpon().appendQueryParameter(TOKEN_QUERY_PARAM, "true").build()
        }

        private fun hasNeedAkamaiToken(uri: Uri): Boolean {
            return uri.getQueryParameter(TOKEN_QUERY_PARAM)?.toBoolean() == true
        }

        private fun removeTokenQueryParameter(uri: Uri): Uri {
            val queryParametersNames = uri.queryParameterNames
            val uriBuilder = uri.buildUpon().clearQuery().build().buildUpon()
            for (name in queryParametersNames) {
                if (TOKEN_QUERY_PARAM != name) {
                    uriBuilder.appendQueryParameter(name, uri.getQueryParameter(name))
                }
            }
            return uriBuilder.build()
        }
    }
}
