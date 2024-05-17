/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srg.dataProvider.integrationlayer.data.remote.Segment
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange

internal object SegmentAdapter {

    fun getBlockedTimeRange(segment: Segment): BlockedTimeRange {
        requireNotNull(segment.blockReason)
        return BlockedTimeRange(
            id = segment.urn,
            start = segment.markIn,
            end = segment.markOut,
            reason = segment.blockReason.toString(),
        )
    }

    fun getBlockedTimeRanges(listSegment: List<Segment>?): List<BlockedTimeRange> {
        return listSegment?.filter { it.blockReason != null }?.map {
            getBlockedTimeRange(it)
        } ?: emptyList()
    }
}
