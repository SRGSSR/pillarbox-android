/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

/**
 * Interface for interacting with the Commanders Act SDK.
 *
 * This interface provides methods for sending data to Commanders Act, such as page views, events, and [TCMediaEvent]s. It also allows for managing
 * permanent data and consent services.
 */
interface CommandersAct {
    /**
     * Sends a page view event to Commanders Act.
     *
     * @param pageView The [CommandersActPageView] object representing the page view to send.
     */
    fun sendPageView(pageView: CommandersActPageView)

    /**
     * Sends an event to Commanders Act.
     *
     * @param event The [CommandersActEvent] instance to send.
     */
    fun sendEvent(event: CommandersActEvent)

    /**
     * Sends a [TCMediaEvent] to Commanders Act.
     *
     * @param event The [TCMediaEvent] to send.
     */
    fun sendTcMediaEvent(event: TCMediaEvent)

    /**
     * Enables the tracking to continue running in the background.
     *
     * When the application is going to background, the ServerSide module sends all data that was already queued then stops. This helps preserve
     * battery life and not use carrier data when not required. But some applications need to be able to continue sending data because they have real
     * background activities, for example, listening to music.
     */
    fun enableRunningInBackground() {}

    /**
     * Puts the provided labels as permanent data.
     *
     * @param labels A map containing the labels.
     */
    fun putPermanentData(labels: Map<String, String>)

    /**
     * Removes permanent data associated with the given label.
     *
     * @param label The label to remove.
     */
    fun removePermanentData(label: String)

    /**
     * Retrieves a permanent data label associated with the given label.
     *
     * @param label The input label for which to retrieve the permanent data label.
     * @return The permanent data label associated with the input label, or `null` if not found.
     */
    fun getPermanentDataLabel(label: String): String?

    /**
     * Sets the list of consent services.
     *
     * @param consentServices The list of consent services granted by the user.
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
