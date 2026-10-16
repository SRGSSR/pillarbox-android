/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.data

import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlinx.serialization.Serializable

/**
 * Time range representation exchanged in the Cast `customData`, shared with the SRG SSR web receiver.
 *
 * Its [type] identifies which kind of [Credit] it represents. Unknown types are ignored.
 */
@Serializable
internal data class CastTimeRange(
    val startTime: Long,
    val endTime: Long,
    val type: String? = null,
) {
    fun toCredit(): Credit? {
        return when (type) {
            OPENING_CREDITS -> Credit.Opening(start = startTime, end = endTime)
            CLOSING_CREDITS -> Credit.Closing(start = startTime, end = endTime)
            else -> null
        }
    }

    companion object {
        const val OPENING_CREDITS = "OPENING_CREDITS"
        const val CLOSING_CREDITS = "CLOSING_CREDITS"

        fun fromCredit(credit: Credit): CastTimeRange {
            val type = when (credit) {
                is Credit.Opening -> OPENING_CREDITS
                is Credit.Closing -> CLOSING_CREDITS
            }
            return CastTimeRange(startTime = credit.start, endTime = credit.end, type = type)
        }
    }
}
