/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * Interface for interacting with the ComScore SDK.
 *
 * This interface provides methods for managing permanent data and user consent.
 */
interface ComScore {

    /**
     * Puts the provided labels as persistent data.
     *
     * @param labels A map containing the labels.
     */
    fun putPersistentLabels(labels: Map<String, String>)

    /**
     * Removes a persistent label.
     *
     * @param label The label to remove.
     */
    fun removePersistentLabel(label: String)

    /**
     * Retrieves the persistent value associated with the given label.
     *
     * @param label The label used to identify the persistent value.
     * @return The persistent value associated with the label, or `null` if not found.
     */
    fun getPersistentLabel(label: String): String?

    /**
     * Sets the user consent.
     *
     * @param userConsent The user consent level, represented by a [ComScoreUserConsent] enum entry.
     */
    fun setUserConsent(userConsent: ComScoreUserConsent)
}

internal object NoOpComScore : ComScore {

    override fun putPersistentLabels(labels: Map<String, String>) = Unit

    override fun removePersistentLabel(label: String) = Unit

    override fun getPersistentLabel(label: String): String? = null

    override fun setUserConsent(userConsent: ComScoreUserConsent) = Unit
}
