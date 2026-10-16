/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.data

import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlinx.serialization.Serializable

/**
 * Blocked Segment representation exchanged in the Cast `customData`, shared with the SRG SSR web receiver.
 */
@Serializable
internal data class CastBlockedSegment(
    val startTime: Long,
    val endTime: Long,
    val identifier: String? = null,
    val blockReason: String? = null,
) {
    fun toBlockedTimeRange(): BlockedTimeRange {
        return BlockedTimeRange(
            id = identifier,
            start = startTime,
            end = endTime,
            reason = blockReason,
        )
    }

    companion object {
        fun fromBlockedTimeRange(blockedTimeRange: BlockedTimeRange): CastBlockedSegment {
            return CastBlockedSegment(
                startTime = blockedTimeRange.start,
                endTime = blockedTimeRange.end,
                identifier = blockedTimeRange.id,
                blockReason = blockedTimeRange.reason,
            )
        }
    }
}
