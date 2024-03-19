/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaItemTrackerDataTest {
    @Test
    fun `media item tracker data`() {
        val emptyMediaItemTrackerData = MediaItemTrackerData.EMPTY
        val mediaItemTracker1 = MediaItemTracker1()
        val mediaItemTracker2 = MediaItemTracker2()

        assertTrue(emptyMediaItemTrackerData.trackers.isEmpty())
        assertTrue(emptyMediaItemTrackerData.isEmpty)
        assertFalse(emptyMediaItemTrackerData.isNotEmpty)
        assertNull(emptyMediaItemTrackerData.getData(mediaItemTracker1))
        assertNull(emptyMediaItemTrackerData.getDataAs(mediaItemTracker1))
        assertNull(emptyMediaItemTrackerData.getData(mediaItemTracker2))
        assertNull(emptyMediaItemTrackerData.getDataAs(mediaItemTracker2))

        val mediaItemTrackerDataUpdated = emptyMediaItemTrackerData.buildUpon()
            .putData(mediaItemTracker1::class.java, "Some value")
            .putData(mediaItemTracker2::class.java)
            .build()

        assertEquals(setOf(mediaItemTracker1::class.java, mediaItemTracker2::class.java), mediaItemTrackerDataUpdated.trackers)
        assertFalse(mediaItemTrackerDataUpdated.isEmpty)
        assertTrue(mediaItemTrackerDataUpdated.isNotEmpty)
        assertEquals("Some value", mediaItemTrackerDataUpdated.getData(mediaItemTracker1))
        assertEquals("Some value", mediaItemTrackerDataUpdated.getDataAs(mediaItemTracker1))
        assertNull(mediaItemTrackerDataUpdated.getData(mediaItemTracker2))
        assertNull(mediaItemTrackerDataUpdated.getDataAs(mediaItemTracker2))
    }

    @Test
    fun `empty media item tracker data are equals`() {
        assertEquals(MediaItemTrackerData.EMPTY, MediaItemTrackerData.Builder().build())
    }

    @Test
    fun `media item tracker data are equals`() {
        val mediaItemTrackerData1 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data1")
            .putData(MediaItemTracker2::class.java, "Data2")
            .build()
        val mediaItemTrackerData2 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data1")
            .putData(MediaItemTracker2::class.java, "Data2")
            .build()
        assertEquals(mediaItemTrackerData1, mediaItemTrackerData2)
    }

    @Test
    fun `media item tracker data are not equals when data changes`() {
        val mediaItemTrackerData1 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data1")
            .putData(MediaItemTracker2::class.java, "Data2")
            .build()
        val mediaItemTrackerData2 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data1")
            .build()
        assertNotEquals(mediaItemTrackerData1, mediaItemTrackerData2)
        val mediaItemTrackerData3 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data1")
        val mediaItemTrackerData4 = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker1::class.java, "Data2")
        assertNotEquals(mediaItemTrackerData3, mediaItemTrackerData4)
    }

    private open class EmptyMediaItemTracker : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) = Unit

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) = Unit
    }

    private class MediaItemTracker1 : EmptyMediaItemTracker()

    private class MediaItemTracker2 : EmptyMediaItemTracker()
}
