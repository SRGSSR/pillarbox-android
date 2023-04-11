/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Page view analytics
 */
interface PageViewAnalytics {
    /**
     * Send page view
     *
     * @param pageView the [PageView] to send.
     */
    fun sendPageView(pageView: PageView)
}
