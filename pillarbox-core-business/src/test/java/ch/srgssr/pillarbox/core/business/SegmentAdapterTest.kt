/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.BlockReason
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Segment
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.core.business.source.SegmentAdapter
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentAdapterTest {
    @Test(expected = IllegalArgumentException::class)
    fun `getBlockedInterval of a non blocked segment`() {
        val segmentIl = createSegment(
            urn = "urn1",
            title = "title 1",
            markIn = 1L,
            markOut = 2L,
            blockReason = null,
        )
        SegmentAdapter.getBlockedTimeRange(segmentIl)
    }

    @Test
    fun `getBlockedInterval of a blocked segment`() {
        val segmentIl = createSegment(
            urn = "urn1",
            title = "title 1",
            markIn = 1L,
            markOut = 2L,
            blockReason = BlockReason.UNKNOWN,
        )
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
            createSegment(
                urn = "urn1",
                title = "title 1",
                markIn = 1L,
                markOut = 2L,
            ),
            createSegment(
                urn = "urn2",
                title = "title 2",
                markIn = 3L,
                markOut = 4L,
            ),
        )
        assertEquals(emptyList(), SegmentAdapter.getBlockedTimeRanges(listSegments))
    }

    @Test
    fun `list of segment with blocked segments return blocked interval list`() {
        val listSegments = listOf(
            createSegment(
                urn = "urn1",
                title = "title 1",
                markIn = 1L,
                markOut = 2L,
            ),
            createSegment(
                urn = "urn1_blocked",
                title = "title 1 blocked",
                markIn = 1L,
                markOut = 4L,
                blockReason = BlockReason.LEGAL,
            ),
            createSegment(
                urn = "urn2",
                title = "title 2",
                markIn = 3L,
                markOut = 4L,
            ),
            createSegment(
                urn = "urn3",
                title = "title 3",
                markIn = 5L,
                markOut = 56L,
                blockReason = BlockReason.UNKNOWN,
            ),
        )
        val expected = listOf(
            BlockedTimeRange(id = "urn1_blocked", start = 1, end = 4, reason = "LEGAL"),
            BlockedTimeRange(id = "urn3", start = 5, end = 56, reason = "UNKNOWN"),
        )
        assertEquals(expected, SegmentAdapter.getBlockedTimeRanges(listSegments))
    }

    private companion object {
        private fun createSegment(
            urn: String,
            title: String,
            markIn: Long,
            markOut: Long,
            blockReason: BlockReason? = null,
        ): Segment {
            return Segment(
                id = "id",
                mediaType = MediaType.VIDEO,
                vendor = Vendor.RTS,
                urn = urn,
                title = title,
                markIn = markIn,
                markOut = markOut,
                type = Type.CLIP,
                date = Clock.System.now(),
                duration = 0L,
                displayable = true,
                playableAbroad = true,
                imageUrl = ImageUrl(""),
                blockReason = blockReason,
            )
        }
    }
}
