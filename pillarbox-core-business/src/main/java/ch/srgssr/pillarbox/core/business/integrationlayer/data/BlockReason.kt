/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Represents the reason why a [Chapter] is blocked.
 */
enum class BlockReason {
    /**
     * The [Chapter] is blocked due to geographical restrictions.
     */
    GEOBLOCK,

    /**
     * The [Chapter] is blocked due to legal reasons.
     */
    LEGAL,

    /**
     * The [Chapter] is blocked for commercial reason.
     */
    COMMERCIAL,

    /**
     * The [Chapter] is blocked due to an age rating of 18.
     */
    AGERATING18,

    /**
     * The [Chapter] is blocked due to an age rating of 12.
     */
    AGERATING12,

    /**
     * The [Chapter] is blocked due to its start date being in the future
     */
    STARTDATE,

    /**
     * The [Chapter] is blocked due to reaching its end date.
     */
    ENDDATE,

    /**
     * The [Chapter] is blocked for journalistic reason.
     */
    JOURNALISTIC,

    /**
     * The [Chapter] is blocked for an unknown reason.
     */
    UNKNOWN,
}
