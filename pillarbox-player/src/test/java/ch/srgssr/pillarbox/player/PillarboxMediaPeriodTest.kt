/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.test.utils.FakeMediaPeriod
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.source.PillarboxMediaPeriod
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.PILLARBOX_BLOCKED_MIME_TYPE
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.PILLARBOX_TRACKERS_MIME_TYPE
import ch.srgssr.pillarbox.player.tracker.FactoryData
import ch.srgssr.pillarbox.player.tracker.FakeMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import io.mockk.mockk
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class PillarboxMediaPeriodTest {

    @Test
    fun `test track group with no tracker data and no blocked time range`() {
        val mediaItemTrackData = MediaItemTrackerData(MutableMediaItemTrackerData.EMPTY)
        val blockedTimeRangeList = emptyList<BlockedTimeRange>()
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = createFakeChildMediaPeriod(),
            mediaItemTrackerData = mediaItemTrackData,
            blockedTimeRanges = blockedTimeRangeList
        )
        val expectedTrackGroup = TrackGroupArray(
            TrackGroup(createDummyFormat("DummyId"))
        )
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assert(mediaPeriod.trackGroups == expectedTrackGroup)
    }

    @Test
    fun `test track group with tracker data and blocked time range`() {
        val mutableMediaItemTrackerData = MutableMediaItemTrackerData()
        mutableMediaItemTrackerData[Any()] = FactoryData(FakeMediaItemTracker.Factory(FakeMediaItemTracker()), FakeMediaItemTracker.Data("Test01"))
        val mediaItemTrackerData = mutableMediaItemTrackerData.toMediaItemTrackerData()
        val blockedTimeRangeList = listOf(BlockedTimeRange(0L, 100L), BlockedTimeRange(200L, 300L))
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = createFakeChildMediaPeriod(),
            mediaItemTrackerData = mediaItemTrackerData,
            blockedTimeRanges = blockedTimeRangeList
        )
        val expectedTrackGroup = TrackGroupArray(
            TrackGroup(createDummyFormat("DummyId")),
            TrackGroup(
                "Pillarbox-Trackers",
                Format.Builder()
                    .setId("TrackerData:0")
                    .setSampleMimeType(PILLARBOX_TRACKERS_MIME_TYPE)
                    .setCustomData(mediaItemTrackerData)
                    .build()
            ),
            TrackGroup(
                "Pillarbox-BlockedTimeRanges",
                Format.Builder()
                    .setSampleMimeType(PILLARBOX_BLOCKED_MIME_TYPE)
                    .setId("BlockedTimeRanges")
                    .setCustomData(blockedTimeRangeList)
                    .build(),
            )
        )
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assert(mediaPeriod.trackGroups == expectedTrackGroup)
    }

    @Test
    fun `test track group with tracker data only`() {
        val mutableMediaItemTrackerData = MutableMediaItemTrackerData()
        mutableMediaItemTrackerData[Any()] = FactoryData(FakeMediaItemTracker.Factory(FakeMediaItemTracker()), FakeMediaItemTracker.Data("Test01"))
        val mediaItemTrackerData = mutableMediaItemTrackerData.toMediaItemTrackerData()
        val blockedTimeRangeList = emptyList<BlockedTimeRange>()
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = createFakeChildMediaPeriod(),
            mediaItemTrackerData = mediaItemTrackerData,
            blockedTimeRanges = blockedTimeRangeList
        )
        val expectedTrackGroup = TrackGroupArray(
            TrackGroup(createDummyFormat("DummyId")),
            TrackGroup(
                "Pillarbox-Trackers",
                Format.Builder()
                    .setId("TrackerData:0")
                    .setSampleMimeType(PILLARBOX_TRACKERS_MIME_TYPE)
                    .setCustomData(mediaItemTrackerData)
                    .build()
            )
        )
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assert(mediaPeriod.trackGroups == expectedTrackGroup)
    }

    @Test
    fun `test track group with blocked time range only`() {
        val mediaItemTrackData = MediaItemTrackerData(MutableMediaItemTrackerData.EMPTY)
        val blockedTimeRangeList = listOf(BlockedTimeRange(0L, 100L), BlockedTimeRange(200L, 300L))
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = createFakeChildMediaPeriod(),
            mediaItemTrackerData = mediaItemTrackData,
            blockedTimeRanges = blockedTimeRangeList
        )
        val expectedTrackGroup = TrackGroupArray(
            TrackGroup(createDummyFormat("DummyId")),
            TrackGroup(
                "Pillarbox-BlockedTimeRanges",
                Format.Builder()
                    .setSampleMimeType(PILLARBOX_BLOCKED_MIME_TYPE)
                    .setId("BlockedTimeRanges")
                    .setCustomData(blockedTimeRangeList)
                    .build(),
            )
        )
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assert(mediaPeriod.trackGroups == expectedTrackGroup)
    }

    companion object {
        fun createFakeChildMediaPeriod(trackGroupArray: TrackGroupArray = createFakeTracks()) =
            FakeMediaPeriod(trackGroupArray, mockk(relaxed = true), 0L, mockk(relaxed = true))

        fun createDummyFormat(id: String) = Format.Builder()
            .setId(id)
            .build()

        private fun createFakeTracks(): TrackGroupArray {
            return TrackGroupArray(
                TrackGroup(
                    createDummyFormat("DummyId")
                )
            )
        }
    }
}
