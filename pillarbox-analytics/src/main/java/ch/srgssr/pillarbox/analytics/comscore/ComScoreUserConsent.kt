/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

/**
 * Represents the user consent status for ComScore.
 */
enum class ComScoreUserConsent {
    /**
     * Represents a status that is unknown or has not yet been determined. This typically serves as a default or initial state before an action is
     * taken.
     */
    UNKNOWN,

    /**
     * Indicates that the user has explicitly given consent.
     */
    ACCEPTED,

    /**
     * Indicates that the user has explicitly declined the request or opted out.
     */
    DECLINED,
}
