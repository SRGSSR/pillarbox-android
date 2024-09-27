/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * ComScore
 *
 * @constructor Create empty Com score
 */
interface ComScore {
    /**
     * Send page view to ComScore
     * @param pageView The [ComScorePageView] to send.
     */
    fun sendPageView(pageView: ComScorePageView)

    /**
     * Put persistent labels
     *
     * @param labels The values to put.
     */
    fun putPersistentLabels(labels: Map<String, String>)

    /**
     * Remove persistent label
     *
     * @param label The label to remove.
     */
    fun removePersistentLabel(label: String)

    /**
     * Get persistent label
     *
     * @param label The label to get.
     * @return null if not found.
     */
    fun getPersistentLabel(label: String): String?

    /**
     * Set user consent
     *
     * @param userConsent
     */
    fun setUserConsent(userConsent: ComScoreUserConsent)
}

internal object NoOpComScore : ComScore {

    override fun sendPageView(pageView: ComScorePageView) = Unit

    override fun putPersistentLabels(labels: Map<String, String>) = Unit

    override fun removePersistentLabel(label: String) = Unit

    override fun getPersistentLabel(label: String): String? = null

    override fun setUserConsent(userConsent: ComScoreUserConsent) = Unit
}
