/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.data

import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import kotlinx.serialization.Serializable

/**
 * Credit representation exchanged in the Cast `customData`, shared with the SRG SSR web receiver.
 */
@Serializable
internal data class CastTimeRange(
    val startTime: Long,
    val endTime: Long,
    val type: String? = null,
) {
    fun toCredit(): Credit? {
        return when (type) {
            OPENING_CREDITS ->
                Credit.Opening(
                    start = startTime,
                    end = endTime
                )

            CLOSING_CREDITS ->
                Credit.Closing(
                    start = startTime,
                    end = endTime
                )

            else -> null
        }
    }

    companion object {
        const val OPENING_CREDITS = "OPENING_CREDITS"
        const val CLOSING_CREDITS = "CLOSING_CREDITS"
        fun fromCredit(credit: Credit): CastTimeRange? {
            return when (credit) {
                is Credit.Opening -> CastTimeRange(
                    type = OPENING_CREDITS,
                    startTime = credit.start,
                    endTime = credit.end,
                )

                is Credit.Closing -> CastTimeRange(
                    type = CLOSING_CREDITS,
                    startTime = credit.start,
                    endTime = credit.end,
                )
            }
        }
    }
}
