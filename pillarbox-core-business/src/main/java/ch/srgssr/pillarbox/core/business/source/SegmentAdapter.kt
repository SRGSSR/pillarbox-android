/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Segment
import ch.srgssr.pillarbox.player.asset.BlockedTimeRange

internal object SegmentAdapter {

    fun getBlockedInterval(segment: Segment): BlockedTimeRange {
        requireNotNull(segment.blockReason)
        return BlockedTimeRange(segment.urn, segment.markIn, segment.markOut, segment.blockReason.toString())
    }

    fun getBlockedIntervals(listSegment: List<Segment>?): List<BlockedTimeRange> {
        return listSegment?.filter { it.blockReason != null }?.map {
            getBlockedInterval(it)
        } ?: emptyList()
    }
}
