/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import android.util.Log
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.SourceKey
import ch.srgssr.pillarbox.analytics.UserConsent
import ch.srgssr.pillarbox.analytics.comscore.ComScoreUserConsent
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Demo application that initializes SRG Analytics and Coil.
 */
class DemoApplication : Application(), SingletonImageLoader.Factory {

    @Suppress("TooGenericExceptionCaught")
    override fun onCreate() {
        super.onCreate()

        // Init Cast shared instance
        val castContext = getCastContext()
        // Update Cast receiver application ID
        val appSettingsRepository = AppSettingsRepository(this)
        MainScope().launch {
            appSettingsRepository.getAppSettings().collect {
                try {
                    castContext.setReceiverApplicationId(it.receiverApplicationId)
                } catch (e: Throwable) {
                    Log.e("PillarboxDemo", "Invalid Cast receiver application ID", e)
                    appSettingsRepository.setReceiverApplicationId(AppSettings.Default)
                }
            }
        }

        // Defaults values
        val initialUserConsent = UserConsent(
            comScore = ComScoreUserConsent.UNKNOWN,
            commandersActConsentServices = emptyList()
        )
        val config = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            nonLocalizedApplicationName = "Pillarbox",
            appSiteName = "pillarbox-demo-android",
            sourceKey = SourceKey.DEVELOPMENT,
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
