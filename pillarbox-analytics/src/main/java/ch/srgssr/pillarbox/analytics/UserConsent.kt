/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScoreUserConsent

/**
 * User consent
 *
 * @property comScore ComScore user consent.
 * @property commandersActConsentServices CommandersAct consent services list.
 */
data class UserConsent(
    val comScore: ComScoreUserConsent = ComScoreUserConsent.UNKNOWN,
    val commandersActConsentServices: List<String> = emptyList()
)
