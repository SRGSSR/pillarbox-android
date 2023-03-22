/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.akamai

import android.net.Uri
import android.text.TextUtils
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.runBlocking

/**
 * Akamai data source that inject Akamai Token when player is opening an Url
 *
 * Create it with [AkamaiTokenDataSource.Factory]
 *
 * @property tokenProvider
 * @property dataSource
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
            outputUri = runBlocking {
                tokenProvider.tokenizeUri(cleanUri)
            }
            return dataSource.open(dataSpec.buildUpon().setUri(outputUri).build())
        }
        return dataSource.open(dataSpec)
    }

    /**
     * Factory that crate a [AkamaiTokenDataSource]
     *
     * @property tokenProvider
     * @property defaultDataSourceFactory by Default [DefaultHttpDataSource]
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
        private fun hasNeedAkamaiToken(uri: Uri): Boolean {
            return uri.getQueryParameter(MediaCompositionMediaItemSource.TOKEN_QUERY_PARAM)?.toBoolean() ?: false
        }

        private fun removeTokenQueryParameter(uri: Uri): Uri {
            val queryParametersNames = uri.queryParameterNames
            val uriBuilder = uri.buildUpon().clearQuery().build().buildUpon()
            for (name in queryParametersNames) {
                if (!TextUtils.equals(MediaCompositionMediaItemSource.TOKEN_QUERY_PARAM, name)) {
                    uriBuilder.appendQueryParameter(name, uri.getQueryParameter(name))
                }
            }
            return uriBuilder.build()
        }
    }
}
