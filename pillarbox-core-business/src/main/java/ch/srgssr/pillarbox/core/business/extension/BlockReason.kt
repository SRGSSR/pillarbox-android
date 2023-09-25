/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import android.content.Context
import ch.srgssr.pillarbox.core.business.R
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason

/**
 * Get string
 *
 * @param blockReason The [BlockReason] to get the string of.
 * @return The string message of [blockReason]
 */
fun Context.getString(blockReason: BlockReason): String {
    return getString(blockReason.getStringResId())
}

/**
 * Get string resource id
 *
 * @return The android string resource id of a [BlockReason]
 */
fun BlockReason.getStringResId(): Int {
    return when (this) {
        BlockReason.AGERATING12 -> R.string.blockReason_ageRating12
        BlockReason.GEOBLOCK -> R.string.blockReason_geoBlock
        BlockReason.LEGAL -> R.string.blockReason_legal
        BlockReason.COMMERCIAL -> R.string.blockReason_commercial
        BlockReason.AGERATING18 -> R.string.blockReason_ageRating18
        BlockReason.STARTDATE -> R.string.blockReason_startDate
        BlockReason.ENDDATE -> R.string.blockReason_endDate
        BlockReason.UNKNOWN -> R.string.blockReason_unknown
    }
}
