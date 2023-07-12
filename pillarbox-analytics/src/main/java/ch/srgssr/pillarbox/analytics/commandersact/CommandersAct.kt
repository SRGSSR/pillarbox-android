/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageView

/**
 * Commanders act interface
 */
interface CommandersAct {
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

    /**
     * Send tc media event to TagCommander.
     *
     * @param event to send
     */
    fun sendTcMediaEvent(event: TCMediaEvent)

    /**
     * Enable running in background
     */
    fun enableRunningInBackground() {}
}
