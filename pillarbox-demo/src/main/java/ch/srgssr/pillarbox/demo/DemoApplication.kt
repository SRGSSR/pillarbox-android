/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.UserConsent
import ch.srgssr.pillarbox.analytics.comscore.ComScoreUserConsent

/**
 * Demo application
 *  - Init SRG Analytics
 */
class DemoApplication : Application() {

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
            sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG,
            userConsent = initialUserConsent
        )
        initSRGAnalytics(config = config)
    }
}
