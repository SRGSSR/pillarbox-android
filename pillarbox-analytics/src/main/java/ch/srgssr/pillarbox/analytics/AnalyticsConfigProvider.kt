/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Analytics config provider that Application must implements.
 */
interface AnalyticsConfigProvider {
    /**
     * Analytics config to use with [SRGAnalytics]
     */
    val analyticsConfig: AnalyticsConfig
}
