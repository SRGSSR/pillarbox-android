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
     * Accepted
     *
     * User has given consent.
     */
    ACCEPTED,

    /**
     * Declined
     *
     * User has not given consent or has opted out.
     */
    DECLINED,
    ;
}
