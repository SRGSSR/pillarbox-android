/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaItemTrackerDataTest {
    @Test
    fun `media item tracker data`() {
        val mediaItemTrackerData = MediaItemTrackerData()
        val mediaItemTracker1 = MediaItemTracker1()
        val mediaItemTracker2 = MediaItemTracker2()

        assertTrue(mediaItemTrackerData.trackers.isEmpty())
        assertNull(mediaItemTrackerData.getData(mediaItemTracker1))
        assertNull(mediaItemTrackerData.getDataAs(mediaItemTracker1))
        assertNull(mediaItemTrackerData.getData(mediaItemTracker2))
        assertNull(mediaItemTrackerData.getDataAs(mediaItemTracker2))

        mediaItemTrackerData.putData(mediaItemTracker1::class.java, "Some value")
        mediaItemTrackerData.putData(mediaItemTracker2::class.java)

        assertEquals(setOf(mediaItemTracker1::class.java, mediaItemTracker2::class.java), mediaItemTrackerData.trackers)
        assertEquals("Some value", mediaItemTrackerData.getData(mediaItemTracker1))
        assertEquals("Some value", mediaItemTrackerData.getDataAs(mediaItemTracker1))
        assertNull(mediaItemTrackerData.getData(mediaItemTracker2))
        assertNull(mediaItemTrackerData.getDataAs(mediaItemTracker2))
    }

    private open class EmptyMediaItemTracker : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) = Unit

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) = Unit
    }

    private class MediaItemTracker1 : EmptyMediaItemTracker()

    private class MediaItemTracker2 : EmptyMediaItemTracker()
}
