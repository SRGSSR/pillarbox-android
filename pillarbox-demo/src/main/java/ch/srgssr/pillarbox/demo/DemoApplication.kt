/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.SourceKey
import ch.srgssr.pillarbox.analytics.UserConsent
import ch.srgssr.pillarbox.analytics.comscore.ComScoreUserConsent
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

/**
 * Demo application that initializes SRG Analytics and Coil.
 */
class DemoApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        // Defaults values
        val initialUserConsent = UserConsent(
            comScore = ComScoreUserConsent.UNKNOWN,
            commandersActConsentServices = emptyList()
        )
        val config = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            nonLocalizedApplicationName = "Pillarbox",
            appSiteName = "pillarbox-demo-android",
            sourceKey = SourceKey.SRG_DEBUG,
            userConsent = initialUserConsent
        )
        initSRGAnalytics(config = config)
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { PillarboxOkHttp() },
                        cacheStrategy = { CacheControlCacheStrategy() },
                    )
                )
            }
            .build()
    }
}
