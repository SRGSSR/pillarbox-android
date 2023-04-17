/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Event analytics
 */
interface EventAnalytics {
    /**
     * Send event
     *
     * @param event the [Event] to send.
     */
    fun sendEvent(event: Event)
}
