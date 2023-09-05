/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
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
    fun putPersistentLabels(labels: Map<String, String>? = null)

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
}
