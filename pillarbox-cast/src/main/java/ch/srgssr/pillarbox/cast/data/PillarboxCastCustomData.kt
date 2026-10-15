/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.data

import ch.srgssr.pillarbox.cast.CastChapter
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import kotlinx.serialization.Serializable

/**
 * Custom Data representation exchanged in the Cast `customData`, shared with the SRG SSR web receiver.
 */
@Serializable
internal data class PillarboxCastCustomData(
    val server: String? = null,
    val blockedSegments: List<CastBlockedSegment>? = null,
    val chapters: List<CastChapter>? = null,
    val timeRanges: List<CastTimeRange>? = null,
) {
    fun toPillarboxMetadata(): PillarboxMetadata {
        return PillarboxMetadata(
            chapters = chapters?.map { c -> c.toChapter() } ?: emptyList(),
            credits = timeRanges?.mapNotNull { c -> c.toCredit() } ?: emptyList(),
            blockedTimeRanges = blockedSegments?.map { c -> c.toBlockedTimeRange() } ?: emptyList(),
        )
    }
}
