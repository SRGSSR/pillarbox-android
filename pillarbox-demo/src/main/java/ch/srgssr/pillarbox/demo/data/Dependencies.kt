/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenProvider
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.player.PillarboxPlayer
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.chromium.net.CronetEngine
import java.io.File
import java.util.concurrent.Executors

/**
 * Dependencies to make custom Dependency Injection
 */
object Dependencies {
    private val DefaultNetworkStack = NetworkStack.Cronet
    private const val MaxCacheSizeBytes: Long = 16 * 1024 * 1024
    private val tokenProvider: AkamaiTokenProvider = AkamaiTokenProvider()
    private var cronetEngine: CronetEngine? = null
    private var okHttpClient: OkHttpClient? = null

    private fun provideIntegrationLayerItemSource(context: Context): MediaCompositionMediaItemSource =
        MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(IlHost.PROD, provideOkHttpClientSingleton(context)))

    /**
     * Provide mixed item source that load Url and Urn
     */
    fun provideMixedItemSource(context: Context): MixedMediaItemSource = MixedMediaItemSource(
        provideIntegrationLayerItemSource(context)
    )

    /**
     * Provide default player that allow to play urls and urns content from the SRG
     */
    fun provideDefaultPlayer(context: Context, networkStack: NetworkStack = DefaultNetworkStack): PillarboxPlayer {
        val dataSourceFactory = when (networkStack) {
            NetworkStack.Cronet -> {
                val cronetEngine = provideCronetSingleton(context)
                val baseDataSource = CronetDataSource.Factory(cronetEngine, Executors.newCachedThreadPool())
                AkamaiTokenDataSource.Factory(tokenProvider, DefaultDataSource.Factory(context, baseDataSource))
            }
            NetworkStack.OkHttp -> {
                val okHttpClient = provideOkHttpClientSingleton(context)
                val baseDataSource = OkHttpDataSource.Factory(okHttpClient)
                AkamaiTokenDataSource.Factory(tokenProvider, DefaultDataSource.Factory(context, baseDataSource))
            }
            else -> {
                AkamaiTokenDataSource.Factory()
            }
        }

        return PillarboxPlayer(
            context = context,
            mediaItemSource = provideMixedItemSource(context),
            /**
             * Optional, only needed if you plan to play akamai token protected content
             */
            dataSourceFactory = dataSourceFactory
        )
    }

    /**
     * Provide OkHttpClient support Http2
     */
    private fun provideOkHttpClientSingleton(context: Context): OkHttpClient {
        if (okHttpClient == null) {
            val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            okHttpClient = OkHttpClient.Builder()
                .cache(Cache(File(context.cacheDir, "pillarbox-cache"), MaxCacheSizeBytes))
                // .addInterceptor(AkamaiTokenOkHttpInterceptor(tokenProvider))
                .addInterceptor(HttpLoggingInterceptor().setLevel(level))
                .build()
        }
        return okHttpClient!!
    }

    /**
     * Provide CronetEngine support Http3/Quic
     *
     * This network stack may improve network call response.
     */
    private fun provideCronetSingleton(context: Context): CronetEngine {
        if (cronetEngine == null) {
            return CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .enableBrotli(true)
                .build()
        }
        return cronetEngine!!
    }
}
