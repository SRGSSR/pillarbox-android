/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaItemTrackerListTest {
    @Test
    fun `empty tracker list`() {
        val trackers = MediaItemTrackerList()
        assertTrue(trackers.isEmpty())
        assertEquals(0, trackers.count())
        assertEquals(emptyList(), trackers.trackerList)
    }

    @Test
    fun `add single tracker`() {
        val trackers = MediaItemTrackerList()
        val tracker = ItemTrackerA()
        assertTrue(trackers.add(tracker))
        assertFalse(trackers.isEmpty())
        assertEquals(1, trackers.count())
        assertEquals(listOf(tracker), trackers.trackerList)
    }

    @Test
    fun `add same kind of tracker multiple times`() {
        val trackers = MediaItemTrackerList()
        val trackerA = ItemTrackerA()
        val trackerAA = ItemTrackerA()
        assertTrue(trackers.add(trackerA))
        assertFalse(trackers.add(trackerAA))
        assertFalse(trackers.isEmpty())
        assertEquals(1, trackers.count())
        assertEquals(listOf(trackerA), trackers.trackerList)
    }

    @Test
    fun `add different kind of trackers`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerA(), ItemTrackerB(), ItemTrackerC())
        for (tracker in trackerList) {
            assertTrue(trackers.add(tracker))
        }

        assertFalse(trackers.isEmpty())
        assertEquals(trackerList.size, trackers.count())
        assertEquals(trackerList, trackers.trackerList)
    }

    @Test
    fun `add different kind of trackers with open tracker`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerC(), ItemTrackerD())
        for (tracker in trackerList) {
            assertTrue(trackers.add(tracker))
        }

        assertFalse(trackers.isEmpty())
        assertEquals(trackerList.size, trackers.count())
        assertEquals(trackerList, trackers.trackerList)

        val trackersRevert = MediaItemTrackerList()
        val trackerListRevert = listOf(ItemTrackerD(), ItemTrackerC())
        for (tracker in trackerListRevert) {
            assertTrue(trackersRevert.add(tracker))
        }

        assertFalse(trackerListRevert.isEmpty())
        assertEquals(trackerListRevert.size, trackersRevert.count())
        assertEquals(trackerListRevert, trackersRevert.trackerList)
    }

    @Test
    fun `add multiple trackers`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerA(), ItemTrackerB(), ItemTrackerA(), ItemTrackerC())
        val expectedTrackers = trackerList.distinctBy { it::class.java }
        assertFalse(trackers.addAll(trackerList))
        assertEquals(expectedTrackers.size, trackers.count())
        assertEquals(expectedTrackers, trackers.trackerList)
    }

    private open class EmptyItemTracker : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) {
            // Nothing
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
            // Nothing
        }
    }

    private class ItemTrackerA : EmptyItemTracker()

    private class ItemTrackerB : EmptyItemTracker()

    private open class ItemTrackerC : EmptyItemTracker()

    private class ItemTrackerD : ItemTrackerC()
}
