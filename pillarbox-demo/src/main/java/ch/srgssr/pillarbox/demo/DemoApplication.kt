/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActLabels
import ch.srgssr.pillarbox.analytics.comscore.ComScoreLabel

/**
 * Demo application
 *  - Init SRG Analytics
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            nonLocalizedApplicationName = "Pillarbox",
            appSiteName = "pillarbox-demo-android",
            sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG,
            commandersActPersistentLabels = mapOf(Pair(CommandersActLabels.CONSENT_SERVICES.label, "service1,service2")),
            comScorePersistentLabels = mapOf(Pair(ComScoreLabel.USER_CONSENT, ""))
        )
        initSRGAnalytics(config = config)
    }
}
