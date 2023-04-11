/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Analytics base interface
 */
interface AnalyticsDelegate {
    /**
     * Send page view
     *
     * @param pageView the [PageView] to send.
     */
    fun sendPageView(pageView: PageView)

    /**
     * Send event
     *
     * @param event the [Event] to send.
     */
    fun sendEvent(event: Event)
}
