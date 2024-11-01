/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScoreUserConsent

/**
 * Represents the user consent for various data collection and processing purposes.
 *
 * @property comScore The user consent for ComScore data collection. Defaults to [ComScoreUserConsent.UNKNOWN].
 * @property commandersActConsentServices A list of consent services for Commanders Act. Defaults to an empty list.
 */
data class UserConsent(
    val comScore: ComScoreUserConsent = ComScoreUserConsent.UNKNOWN,
    val commandersActConsentServices: List<String> = emptyList()
)
