/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.app.Application
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.AnalyticsConfigProvider

/**
 * Demo application
 *  - Init SRG Analytics
 */
class DemoApplication : Application(), AnalyticsConfigProvider {

    override val analyticsConfig: AnalyticsConfig
        get() = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            nonLocalizedApplicationName = "PillarboxDemo",
            virtualSite = "pillarbox-demo-android",
            sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
        )
}
