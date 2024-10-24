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
 * Provide a Ktor [OkHttpClient] instance tailored for Pillarbox's needs.
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
     * Returns the [OkHttpClient] tailored for Pillarbox's needs.
     *
     * @return A [OkHttpClient] instance.
     */
    operator fun invoke(): OkHttpClient {
        return okHttpClient
    }
}
