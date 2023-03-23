/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * Analytics base interface
 */
interface Analytics {
    /**
     * Send page view event
     *
     * @param pageEvent the [PageEvent].
     */
    fun sendPageViewEvent(pageEvent: PageEvent)

    /**
     * Send event
     *
     * @param event the [Event] to send.
     */
    fun sendEvent(event: Event)
}
