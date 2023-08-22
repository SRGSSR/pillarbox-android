/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.content.Context
import android.content.res.Configuration
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl.Companion.getVector
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Implementation of MediaCompositionDataSource using integration layer.
 *
 * @property mediaCompositionService MediaCompositionService implementation.
 * @property vector The distribution vector. see [Context.getVector].
 */
class MediaCompositionDataSourceImpl(
    private val mediaCompositionService: MediaCompositionService,
    private val vector: String? = VECTOR_MOBILE
) : MediaCompositionDataSource {
    constructor(host: URL, okHttpClient: OkHttpClient, vector: String? = VECTOR_MOBILE) :
        this(createMediaCompositionService(host, okHttpClient), vector)

    constructor(context: Context, host: URL, vector: String? = context.getVector()) :
        this(host, createOkHttpClient(context), vector)

    override suspend fun getMediaCompositionByUrn(urn: String): RemoteResult<MediaComposition> {
        return try {
            val result = mediaCompositionService.getMediaCompositionByUrn(urn = urn, vector = vector, onlyChapters = true)
            RemoteResult.Success(result)
        } catch (e: HttpException) {
            RemoteResult.Error(e, e.code())
        } catch (e: IOException) {
            RemoteResult.Error(e)
        }
    }

    companion object {
        private var READ_TIMEOUT_SECONDS: Long = 60L
        private var CONNECT_TIMEOUT_SECONDS: Long = 60L
        private var DEFAULT_CACHE_DIR = "il_cache"
        private var DEFAULT_CACHE_MAX_SIZE = 2 * 1024 * 1024L

        /**
         * Tv Vector
         */
        const val VECTOR_TV = "TVPLAY"

        /**
         * Mobile Vector
         */
        const val VECTOR_MOBILE = "APPPLAY"

        private fun createMediaCompositionService(ilHost: URL, okHttpClient: OkHttpClient): MediaCompositionService {
            return Retrofit.Builder()
                .baseUrl(ilHost)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(MediaCompositionService::class.java)
        }

        private fun createDefaultCache(context: Context): Cache {
            return Cache(File(context.cacheDir, DEFAULT_CACHE_DIR), DEFAULT_CACHE_MAX_SIZE)
        }

        private fun createOkHttpClient(
            context: Context
        ): OkHttpClient {
            val builder = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            builder.cache(createDefaultCache(context))
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS)
            builder.addInterceptor(logging)
            builder.readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            builder.connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            return builder.build()
        }

        /**
         * Get vector
         *
         * @return vector for MediaCompositionService.
         */
        fun Context.getVector(): String {
            val uiMode = resources.configuration.uiMode
            return if (uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION) {
                VECTOR_TV
            } else {
                VECTOR_MOBILE
            }
        }
    }
}
