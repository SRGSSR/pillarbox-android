/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.MediaPeriod
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
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PillarboxMediaPeriodTest {
    private lateinit var format: Format
    private lateinit var mediaPeriod: MediaPeriod

    @BeforeTest
    fun setUp() {
        format = Format.Builder()
            .setId("FakeId")
            .build()
        mediaPeriod = FakeMediaPeriod(
            /* trackGroupArray = */ TrackGroupArray(TrackGroup(format)),
            /* allocator = */ mockk(relaxed = true),
            /* singleSampleTimeUs = */ 0L,
            /* mediaSourceEventDispatcher = */ mockk(relaxed = true),
        )
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `check all default method are implemented`() {
        val defaultMethod = MediaPeriod::class.java.declaredMethods.filter { it.isDefault }
        for (method in defaultMethod) {
            val name = method.name
            val parameters = method.parameterTypes
            assertEquals(PillarboxMediaPeriod::class.java, PillarboxMediaPeriod::class.java.getDeclaredMethod(name, *parameters).declaringClass)
        }
    }

    @Test
    fun `test track group with no tracker data and no blocked time range`() {
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = mediaPeriod,
            mediaItemTrackerData = emptyTrackerData,
            blockedTimeRanges = emptyList(),
        )
        val expectedTrackGroup = TrackGroupArray(TrackGroup(format))
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assertEquals(expectedTrackGroup, mediaPeriod.trackGroups)
    }

    @Test
    fun `test track group with tracker data and blocked time range`() {
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = mediaPeriod,
            mediaItemTrackerData = trackerData,
            blockedTimeRanges = blockedTimeRanges,
        )
        val expectedTrackGroup = TrackGroupArray(TrackGroup(format), trackTrackers, trackBlockedTimeRanges)
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assertEquals(expectedTrackGroup, mediaPeriod.trackGroups)
    }

    @Test
    fun `test track group with tracker data only`() {
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = mediaPeriod,
            mediaItemTrackerData = trackerData,
            blockedTimeRanges = emptyList(),
        )
        val expectedTrackGroup = TrackGroupArray(TrackGroup(format), trackTrackers)
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assertEquals(expectedTrackGroup, mediaPeriod.trackGroups)
    }

    @Test
    fun `test track group with blocked time range only`() {
        val mediaPeriod = PillarboxMediaPeriod(
            mediaPeriod = mediaPeriod,
            mediaItemTrackerData = emptyTrackerData,
            blockedTimeRanges = blockedTimeRanges,
        )
        val expectedTrackGroup = TrackGroupArray(TrackGroup(format), trackBlockedTimeRanges)
        mediaPeriod.prepare(mockk(relaxed = true), 0)
        assertEquals(expectedTrackGroup, mediaPeriod.trackGroups)
    }

    @Test
    fun `test MediaPeriod Callback is forwarded`() {
        val mediaPeriod = PillarboxMediaPeriod(mediaPeriod = mediaPeriod, emptyTrackerData, emptyList())
        val callback = mockk<MediaPeriod.Callback>(relaxed = true)
        mediaPeriod.prepare(callback, 0)

        verify {
            callback.onPrepared(mediaPeriod)
        }

        confirmVerified(callback)
    }

    private companion object {
        private val blockedTimeRanges = listOf(BlockedTimeRange(0L, 100L), BlockedTimeRange(200L, 300L))
        private val emptyTrackerData = MediaItemTrackerData(MutableMediaItemTrackerData.EMPTY)
        private val trackerData = MutableMediaItemTrackerData().apply {
            put(Any(), FactoryData(FakeMediaItemTracker.Factory(FakeMediaItemTracker()), FakeMediaItemTracker.Data("Test01")))
        }.toMediaItemTrackerData()

        private val trackBlockedTimeRanges = TrackGroup(
            "Pillarbox-BlockedTimeRanges",
            Format.Builder()
                .setId("BlockedTimeRanges")
                .setSampleMimeType(PILLARBOX_BLOCKED_MIME_TYPE)
                .setCustomData(blockedTimeRanges)
                .build(),
        )

        private val trackTrackers = TrackGroup(
            "Pillarbox-Trackers",
            Format.Builder()
                .setId("TrackerData:0")
                .setSampleMimeType(PILLARBOX_TRACKERS_MIME_TYPE)
                .setCustomData(trackerData)
                .build(),
        )
    }
}
