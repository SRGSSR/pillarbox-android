/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

/**
 * Commanders act interface
 */
interface CommandersAct {
    /**
     * Send page view
     *
     * @param pageView the [CommandersActPageView] to send.
     */
    fun sendPageView(pageView: CommandersActPageView)

    /**
     * Send event
     *
     * @param event the [CommandersActEvent] to send.
     */
    fun sendEvent(event: CommandersActEvent)

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

    /**
     * Put permanent data
     *
     * @param labels The values to put.
     */
    fun putPermanentData(labels: Map<String, String>)

    /**
     * Remove permanent data
     *
     * @param label The label to remove.
     */
    fun removePermanentData(label: String)

    /**
     * Get permanent label
     *
     * @param label The label to get.
     * @return null if not found.
     */
    fun getPermanentDataLabel(label: String): String?

    /**
     * Set consent services
     *
     * @param consentServices The list of consent services by the user.
     */
    fun setConsentServices(consentServices: List<String>)
}

internal object NoOpCommandersAct : CommandersAct {

    override fun sendPageView(pageView: CommandersActPageView) = Unit

    override fun sendEvent(event: CommandersActEvent) = Unit

    override fun sendTcMediaEvent(event: TCMediaEvent) = Unit

    override fun putPermanentData(labels: Map<String, String>) = Unit

    override fun removePermanentData(label: String) = Unit

    override fun getPermanentDataLabel(label: String): String? = null

    override fun setConsentServices(consentServices: List<String>) = Unit
}
