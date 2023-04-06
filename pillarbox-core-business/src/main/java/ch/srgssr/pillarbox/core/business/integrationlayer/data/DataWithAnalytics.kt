/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Data with analytics
 */
interface DataWithAnalytics {
    /**
     * ComScore analytics labels
     */
    val comScoreAnalyticsLabels: Map<String, String>?

    /**
     * CommandersAct analytics labels
     */
    val analyticsLabels: Map<String, String>?
}
