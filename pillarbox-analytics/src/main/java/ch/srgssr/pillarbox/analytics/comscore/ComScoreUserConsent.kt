/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * Com score user consent
 */
enum class ComScoreUserConsent {
    /**
     * Unknown
     *
     * User has not taken an action.
     */
    UNKNOWN,

    /**
     * Given
     *
     * User has given consent.
     */
    GIVEN,

    /**
     * Refuse
     *
     * User has not given consent or has opted out.
     */
    REFUSED,
    ;
}
