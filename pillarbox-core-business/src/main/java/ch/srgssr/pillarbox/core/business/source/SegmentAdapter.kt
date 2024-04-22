/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Segment
import ch.srgssr.pillarbox.player.asset.BlockedInterval

internal object SegmentAdapter {

    fun getBlockedInterval(segment: Segment): BlockedInterval {
        requireNotNull(segment.blockReason)
        return BlockedInterval(segment.urn, segment.markIn, segment.markOut, segment.blockReason.toString())
    }

    fun getBlockedIntervals(listSegment: List<Segment>?): List<BlockedInterval> {
        return listSegment?.filter { it.blockReason != null }?.map {
            getBlockedInterval(it)
        } ?: emptyList()
    }
}
