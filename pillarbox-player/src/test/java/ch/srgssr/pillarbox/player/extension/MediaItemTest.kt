/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.net.Uri
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.asset.PillarboxData
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

        assertNull(mediaItem.getPillarboxDataOrNull())
        assertTrue(mediaItem.pillarboxData.trackersData.isEmpty)
    }

    @Test
    fun `getMediaItemTrackerData with tag set with wrong type`() {
        val mediaItem = MediaItem.Builder()
            .setTag("Hello, World!")
            .build()

        assertNull(mediaItem.getPillarboxDataOrNull())
        assertTrue(mediaItem.pillarboxData.trackersData.isEmpty)
    }

    @Test
    fun `getMediaItemTrackerData with tag set`() {
        val mediaItemTrackerData = MediaItemTrackerData.Builder()
            .putData(MediaItemTracker::class.java)
            .build()
        val pillarboxData = PillarboxData(mediaItemTrackerData)
        val mediaItem = MediaItem.Builder()
            .setUri(mockk<Uri>())
            .setPillarboxData(pillarboxData)
            .build()

        assertSame(pillarboxData, mediaItem.getPillarboxDataOrNull())
        assertSame(pillarboxData, mediaItem.pillarboxData)
    }
}
