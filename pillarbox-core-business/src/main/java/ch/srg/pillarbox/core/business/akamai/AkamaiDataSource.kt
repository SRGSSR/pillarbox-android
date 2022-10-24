/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.akamai

import android.net.Uri
import android.text.TextUtils
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import kotlinx.coroutines.runBlocking

/**
 * Akamai data source that inject Akamai Token when player is opening an Url
 *
 * @property tokenProvider
 * @property dataSource
 * @constructor Create empty Akamai data source
 */
class AkamaiDataSource(
    private val tokenProvider: AkamaiTokenProvider,
    private val dataSource: DataSource
) : DataSource by dataSource {

    override fun open(dataSpec: DataSpec): Long {
        var outputUri = dataSpec.uri
        if (hasNeedAkamaiToken(outputUri)) {
            val cleanUri = removeTokenQueryParameter(outputUri)
            outputUri = runBlocking {
                tokenProvider.tokenizeUri(cleanUri)
            }
            return dataSource.open(dataSpec.buildUpon().setUri(outputUri).build())
        }
        return dataSource.open(dataSpec)
    }

    /**
     * Factory that crate a [AkamaiDataSource]
     *
     * @property tokenProvider
     * @property defaultDataSourceFactory by Default [DefaultHttpDataSource]
     */
    class Factory(
        private val tokenProvider: AkamaiTokenProvider = AkamaiTokenProvider(),
        private val defaultDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return AkamaiDataSource(tokenProvider, defaultDataSourceFactory.createDataSource())
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
