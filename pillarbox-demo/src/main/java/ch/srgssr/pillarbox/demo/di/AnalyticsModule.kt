/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.di

import android.content.Context
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActConfig

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
            nonLocalizedApplicationName = "PillarboxDemo"
        )
        val commandersActConfig = CommandersActConfig(virtualSite = VIRTUAL_SITE, sourceKey = CommandersActConfig.SOURCE_KEY_SRG_DEBUG)
        val config = SRGAnalytics.Config(analyticsConfig = analyticsConfig, commandersAct = commandersActConfig)
        return SRGAnalytics.init(appContext = appContext, config = config)
    }
}
