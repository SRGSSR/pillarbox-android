/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.di

import android.content.Context
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct

/**
 * Analytics module
 */
object AnalyticsModule {
    private const val VIRTUAL_SITE = "pillarbox-demo-android"

    /**
     * Provider analytics
     */
    fun providerAnalytics(appContext: Context): SRGAnalytics {
        val analyticsConfig = AnalyticsConfig(
            distributor = AnalyticsConfig.BuDistributor.SRG,
            virtualSite = VIRTUAL_SITE,
            nonLocalizedApplicationName = "PillarboxDemo"
        )
        val config = SRGAnalytics.Config(analyticsConfig = analyticsConfig, commandersAct = CommandersAct.Config.SRG_DEBUG)
        return SRGAnalytics(appContext = appContext, config = config)
    }
}
