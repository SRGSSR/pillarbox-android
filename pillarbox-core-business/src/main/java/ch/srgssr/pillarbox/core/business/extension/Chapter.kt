/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.AgeRating12
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.AgeRating18
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.Commercial
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.EndDate
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.GeoBlock
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.Journalistic
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.Legal
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.StartDate
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.Unknown
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException.VPNProxyYDetected
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter

/**
 * Converts the [Chapter.blockReason] into a [BlockReasonException].
 *
 * @return A [BlockReasonException] corresponding to the [Chapter.blockReason] property, or `null` if the [Chapter] is not blocked.
 */
fun Chapter.getBlockReasonExceptionOrNull(): BlockReasonException? {
    return when (blockReason) {
        null -> null
        BlockReason.STARTDATE -> StartDate(instant = validFrom)
        BlockReason.ENDDATE -> EndDate(instant = validTo)
        BlockReason.LEGAL -> Legal()
        BlockReason.AGERATING18 -> AgeRating18()
        BlockReason.AGERATING12 -> AgeRating12()
        BlockReason.GEOBLOCK -> GeoBlock()
        BlockReason.COMMERCIAL -> Commercial()
        BlockReason.JOURNALISTIC -> Journalistic()
        BlockReason.VPNPROXYDETECTED -> VPNProxyYDetected()
        BlockReason.UNKNOWN -> Unknown()
    }
}
