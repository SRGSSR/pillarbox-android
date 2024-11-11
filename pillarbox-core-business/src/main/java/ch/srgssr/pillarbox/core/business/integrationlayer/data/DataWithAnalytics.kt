/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Represents data that can be associated with analytics tracking information.
 *
 * This interface provides properties for storing analytics labels for different analytics providers such as ComScore and Commanders Act.
 */
interface DataWithAnalytics {
    /**
     * Labels for ComScore analytics.
     */
    val comScoreAnalyticsLabels: Map<String, String>?

    /**
     * Labels for Commanders Act analytics.
     */
    val analyticsLabels: Map<String, String>?
}
