/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Block reason
 */
enum class BlockReason {
    GEOBLOCK,
    LEGAL,
    COMMERCIAL,
    AGERATING18,
    AGERATING12,
    STARTDATE,
    ENDDATE,
    UNKNOWN,
}
