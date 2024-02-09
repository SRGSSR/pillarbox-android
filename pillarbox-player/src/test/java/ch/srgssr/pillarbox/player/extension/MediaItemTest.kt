/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.net.Uri
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MediaItemTest {
    @Test
    fun `getMediaItemTrackerData with no tag set`() {
        val mediaItem = MediaItem.Builder().build()

        assertNull(mediaItem.getMediaItemTrackerDataOrNull())
        assertTrue(mediaItem.getMediaItemTrackerData().trackers.isEmpty())
    }

    @Test
    fun `getMediaItemTrackerData with tag set with wrong type`() {
        val mediaItem = MediaItem.Builder()
            .setTag("Hello, World!")
            .build()

        assertNull(mediaItem.getMediaItemTrackerDataOrNull())
        assertTrue(mediaItem.getMediaItemTrackerData().trackers.isEmpty())
    }

    @Test
    fun `getMediaItemTrackerData with tag set`() {
        val mediaItemTrackerData = MediaItemTrackerData.Builder().apply {
            putData(MediaItemTracker::class.java)
        }.build()

        val mediaItem = MediaItem.Builder()
            .setUri(mockk<Uri>())
            .setTrackerData(mediaItemTrackerData)
            .build()

        assertSame(mediaItemTrackerData, mediaItem.getMediaItemTrackerDataOrNull())
        assertSame(mediaItemTrackerData, mediaItem.getMediaItemTrackerData())
    }
}
