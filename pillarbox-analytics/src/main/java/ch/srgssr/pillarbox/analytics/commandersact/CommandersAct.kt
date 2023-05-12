/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.EventAnalytics
import ch.srgssr.pillarbox.analytics.PageViewAnalytics
import ch.srgssr.pillarbox.analytics.UserAnalytics
import com.tagcommander.lib.serverside.events.TCEvent

/**
 * Commanders act interface
 */
interface CommandersAct : PageViewAnalytics, EventAnalytics, UserAnalytics {
    /**
     * Send tc event to TagCommander.
     *
     * @param event to send
     */
    fun sendTcEvent(event: TCEvent)

    /**
     * Enable running in background
     */
    fun enableRunningInBackground() {}
}
