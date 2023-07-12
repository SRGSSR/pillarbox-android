/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics

/**
 * Demo application
 *  - Init SRG Analytics
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initAnalytics()
    }

    private fun initAnalytics() {
        val analyticsConfig = AnalyticsConfig(
            distributor = AnalyticsConfig.BuDistributor.SRG,
            nonLocalizedApplicationName = "PillarboxDemo",
            virtualSite = "pillarbox-demo-android",
            sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
        )
        SRGAnalytics.init(appContext = this, config = analyticsConfig)
    }
}
