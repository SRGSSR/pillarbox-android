/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.network

import ch.srgssr.pillarbox.player.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * Provides a singleton instance of [OkHttpClient] configured for Pillarbox's requirements.
 */
object PillarboxOkHttp {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    val logLevel = if (BuildConfig.DEBUG) Level.BASIC else Level.NONE
                    setLevel(logLevel)
                }
            )
            .build()
    }

    /**
     * Provides access to the pre-configured [OkHttpClient] instance used by Pillarbox.
     *
     * @return The pre-configured [OkHttpClient] instance.
     */
    operator fun invoke(): OkHttpClient {
        return okHttpClient
    }
}
