/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Segment
import ch.srgssr.pillarbox.core.business.source.SegmentAdapter
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentAdapterTest {
    @Test(expected = IllegalArgumentException::class)
    fun `getBlockedInterval of a non blocked segment`() {
        val segmentIl = Segment(urn = "urn1", title = "title 1", markIn = 1, markOut = 2, blockReason = null)
        SegmentAdapter.getBlockedTimeRange(segmentIl)
    }

    @Test
    fun `getBlockedInterval of a blocked segment`() {
        val segmentIl = Segment(urn = "urn1", title = "title 1", markIn = 1, markOut = 2, blockReason = BlockReason.UNKNOWN)
        val expected = BlockedTimeRange(id = "urn1", start = 1, end = 2, reason = "UNKNOWN")
        assertEquals(expected, SegmentAdapter.getBlockedTimeRange(segmentIl))
    }

    @Test
    fun `empty segment list return empty blocked interval`() {
        assertEquals(emptyList(), SegmentAdapter.getBlockedTimeRanges(null))
        assertEquals(emptyList(), SegmentAdapter.getBlockedTimeRanges(emptyList()))
    }

    @Test
    fun `list of non blocked segments return empty blocked interval list`() {
        val listSegments = listOf(
            Segment(urn = "urn1", title = "title 1", markIn = 1, markOut = 2),
            Segment(urn = "urn2", title = "title 2", markIn = 3, markOut = 4),
        )
        assertEquals(emptyList(), SegmentAdapter.getBlockedTimeRanges(listSegments))
    }

    @Test
    fun `list of segment with blocked segments return blocked interval list`() {
        val listSegments = listOf(
            Segment(urn = "urn1", title = "title 1", markIn = 1, markOut = 2),
            Segment(urn = "urn1_blocked", title = "title 1 blocked", markIn = 1, markOut = 4, blockReason = BlockReason.LEGAL),
            Segment(urn = "urn2", title = "title 2", markIn = 3, markOut = 4),
            Segment(urn = "urn3", title = "title 3", markIn = 5, markOut = 56, blockReason = BlockReason.UNKNOWN),
        )
        val expected = listOf(
            BlockedTimeRange(id = "urn1_blocked", start = 1, end = 4, reason = "LEGAL"),
            BlockedTimeRange(id = "urn3", start = 5, end = 56, reason = "UNKNOWN"),
        )
        assertEquals(expected, SegmentAdapter.getBlockedTimeRanges(listSegments))
    }
}
