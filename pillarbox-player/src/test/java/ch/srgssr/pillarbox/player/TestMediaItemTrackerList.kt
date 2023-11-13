/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerList
import org.junit.Assert
import org.junit.Test

class TestMediaItemTrackerList {

    @Test
    fun testEmpty() {
        val trackers = MediaItemTrackerList()
        Assert.assertEquals(trackers.size, 0)
        Assert.assertTrue(trackers.list.isEmpty())
    }

    @Test
    fun testAppendOnce() {
        val trackers = MediaItemTrackerList()
        val tracker = ItemTrackerA()
        val added = trackers.append(tracker)
        Assert.assertTrue(added)
        Assert.assertEquals(trackers.size, 1)
        Assert.assertEquals(trackers.list, listOf(tracker))
    }

    @Test
    fun testAppendTwiceSameKindOfTracker() {
        val trackers = MediaItemTrackerList()
        val trackerA = ItemTrackerA()
        val trackerAA = ItemTrackerA()
        trackers.append(trackerA)
        val added = trackers.append(trackerAA)
        Assert.assertFalse(added)
        Assert.assertEquals(trackers.size, 1)
        Assert.assertEquals(trackers.list, listOf(trackerA))
    }

    @Test
    fun testAppendDifferentTracker() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerA(), ItemTrackerB(), ItemTrackerC())
        for (tracker in trackerList) {
            trackers.append(tracker)
        }

        Assert.assertEquals(trackers.size, trackerList.size)
        Assert.assertEquals(trackers.list, trackerList)
    }

    @Test
    fun testAppendDifferentTrackerWithOpenTracker() {
        val trackers = MediaItemTrackerList()
        val trackerList = listOf(ItemTrackerC(), ItemTrackerD())
        for (tracker in trackerList) {
            trackers.append(tracker)
        }
        Assert.assertEquals(trackers.size, trackerList.size)
        Assert.assertEquals(trackers.list, trackerList)

        val trackersRevert = MediaItemTrackerList()
        val trackerListRevert = listOf(ItemTrackerD(), ItemTrackerC())
        for (tracker in trackerListRevert) {
            trackersRevert.append(tracker)
        }
        Assert.assertEquals(trackersRevert.size, trackerListRevert.size)
        Assert.assertEquals(trackersRevert.list, trackerListRevert)
    }

    @Test
    fun testFindTracker() {
        val trackers = MediaItemTrackerList()
        val tracker = ItemTrackerA()
        val tracker2 = ItemTrackerB()
        trackers.append(tracker)
        trackers.append(tracker2)

        val trackerA = trackers.findTracker(ItemTrackerA::class.java)
        Assert.assertEquals(tracker, trackerA)
        val trackerB = trackers.findTracker(ItemTrackerB::class.java)
        Assert.assertEquals(tracker2, trackerB)

        val trackerC = trackers.findTracker(ItemTrackerC::class.java)
        Assert.assertNull(trackerC)
    }

    private class ItemTrackerA : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) {
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        }

    }

    private class ItemTrackerB : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) {
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        }
    }

    private open class ItemTrackerC : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) {
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        }
    }

    private class ItemTrackerD : ItemTrackerC()
}
