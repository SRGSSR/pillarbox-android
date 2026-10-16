/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.data

import ch.srgssr.pillarbox.player.asset.PillarboxMetadata

/**
 * Pillarbox metadata decoded from the Cast `customData`, shared with the SRG SSR web receiver.
 */
internal data class PillarboxCastCustomData(
    val chapters: List<CastChapter> = emptyList(),
    val blockedSegments: List<CastBlockedSegment> = emptyList(),
    val timeRanges: List<CastTimeRange> = emptyList(),
) {
    fun toPillarboxMetadata(): PillarboxMetadata {
        return PillarboxMetadata(
            chapters = chapters.map(CastChapter::toChapter),
            credits = timeRanges.mapNotNull(CastTimeRange::toCredit),
            blockedTimeRanges = blockedSegments.map(CastBlockedSegment::toBlockedTimeRange),
        )
    }
}
