/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaItemTrackerListTest {
    @Test
    fun `empty tracker list`() {
        val trackers = MediaItemTrackerList()
        assertEquals(0, trackers.size)
        assertTrue(trackers.list.isEmpty())
    }

    @Test
    fun `append single tracker`() {
        val trackers = MediaItemTrackerList()
        val tracker = ItemTrackerA()
        assertTrue(trackers.append(tracker))
        assertEquals(1, trackers.size)
        assertEquals(listOf(tracker), trackers.list)
    }

    @Test
    fun `append same kind of tracker multiple times`() {
        val trackers = MediaItemTrackerList()
        val trackerA = ItemTrackerA()
        val trackerAA = ItemTrackerA()
        assertTrue(trackers.append(trackerA))
        assertFalse(trackers.append(trackerAA))
        assertEquals(1, trackers.size)
        assertEquals(listOf(trackerA), trackers.list)
    }

    @Test
    fun `append different kind of trackers`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerA(), ItemTrackerB(), ItemTrackerC())
        for (tracker in trackerList) {
            assertTrue(trackers.append(tracker))
        }

        assertEquals(trackerList.size, trackers.size)
        assertEquals(trackerList, trackers.list)
    }

    @Test
    fun `append different kind of trackers with open tracker`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerC(), ItemTrackerD())
        for (tracker in trackerList) {
            assertTrue(trackers.append(tracker))
        }
        assertEquals(trackerList.size, trackers.size)
        assertEquals(trackerList, trackers.list)

        val trackersRevert = MediaItemTrackerList()
        val trackerListRevert = listOf(ItemTrackerD(), ItemTrackerC())
        for (tracker in trackerListRevert) {
            assertTrue(trackersRevert.append(tracker))
        }
        assertEquals(trackerListRevert.size, trackersRevert.size)
        assertEquals(trackerListRevert, trackersRevert.list)
    }

    @Test
    fun `appends multiple trackers`() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerA(), ItemTrackerB(), ItemTrackerA(), ItemTrackerC())
        val expectedTrackers = trackerList.distinctBy { it::class.java }
        assertFalse(trackers.appends(*trackerList.toTypedArray()))
        assertEquals(expectedTrackers.size, trackers.size)
        assertEquals(expectedTrackers, trackers.list)
    }

    @Test
    fun `find tracker`() {
        val trackers = MediaItemTrackerList()
        val tracker = ItemTrackerA()
        val tracker2 = ItemTrackerB()
        trackers.append(tracker)
        trackers.append(tracker2)

        val trackerA = trackers.findTracker(ItemTrackerA::class.java)
        assertEquals(tracker, trackerA)

        val trackerB = trackers.findTracker(ItemTrackerB::class.java)
        assertEquals(tracker2, trackerB)

        val trackerC = trackers.findTracker(ItemTrackerC::class.java)
        assertNull(trackerC)
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
