/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.EventAnalytics
import ch.srgssr.pillarbox.analytics.PageViewAnalytics
import ch.srgssr.pillarbox.analytics.UserAnalytics

/**
 * Commanders act interface
 */
interface CommandersAct : PageViewAnalytics, EventAnalytics, UserAnalytics {
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
