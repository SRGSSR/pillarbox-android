/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AnalyticsMediaItemTrackerAreEqualTest {

    @Test
    fun `areEqual both mediaItem are null`() {
        assertTrue(AnalyticsMediaItemTracker.areEqual(null, null))
    }

    @Test
    fun `areEqual first mediaItem is null`() {
        assertFalse(AnalyticsMediaItemTracker.areEqual(null, MediaItem.EMPTY))
    }

    @Test
    fun `areEqual second mediaItem is null`() {
        assertFalse(AnalyticsMediaItemTracker.areEqual(MediaItem.EMPTY, null))
    }

    @Test
    fun `areEqual with different media id without tag and url`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val mediaItem2 = createMediaItemWithMediaId("M2")
        assertFalse(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with same media id without tag and url`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val mediaItem2 = createMediaItemWithMediaId("M1")
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with one default media id`() {
        val mediaItem = createMediaItemWithMediaId(MediaItem.DEFAULT_MEDIA_ID)
        val mediaItem2 = createMediaItemWithMediaId("M1")
        assertFalse(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with both default media id`() {
        val mediaItem = createMediaItemWithMediaId(MediaItem.DEFAULT_MEDIA_ID)
        val mediaItem2 = createMediaItemWithMediaId(MediaItem.DEFAULT_MEDIA_ID)
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with same media id same url same tag`() {
        val mediaId = "M1"
        val url = "https://streaming.com/video.mp4"
        val mediaItem = createMediaItemWithMediaId(mediaId = mediaId, url = url, tag = "Tag1")
        val mediaItem2 = createMediaItemWithMediaId(mediaId = mediaId, url = url, tag = "Tag1")
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with same media id same url without tag`() {
        val mediaId = "M1"
        val url = "https://streaming.com/video.mp4"
        val mediaItem = createMediaItemWithMediaId(mediaId = mediaId, url = url)
        val mediaItem2 = createMediaItemWithMediaId(mediaId = mediaId, url = url)
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with same media id same url and different tag`() {
        val mediaId = "M1"
        val url = "https://streaming.com/video.mp4"
        val mediaItem = createMediaItemWithMediaId(mediaId = mediaId, url = url, tag = null)
        val mediaItem2 = createMediaItemWithMediaId(mediaId = mediaId, url = url, tag = "Tag2")
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual with same media id different url and same tag`() {
        val mediaId = "M1"
        val url = "https://streaming.com/video.mp4"
        val mediaItem = createMediaItemWithMediaId(mediaId = mediaId, url = url, tag = "Tag1")
        val mediaItem2 = createMediaItemWithMediaId(mediaId = mediaId, url = "https://streaming.com/video2.mp4", tag = "Tag1")
        assertFalse(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual no media id same url different tag`() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTag("Tag1")
            .build()

        val mediaItem2 = mediaItem.buildUpon()
            .setTag("Tag2")
            .build()
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual same MediaItemTrackerData content`() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTrackerData(MediaItemTrackerData.Builder().putData(Tracker::class.java, "data1").build())
            .build()

        val mediaItem2 = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTrackerData(MediaItemTrackerData.Builder().putData(Tracker::class.java, "data1").build())
            .build()
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual same MediaItemTrackerData`() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTrackerData(MediaItemTrackerData.Builder().putData(Tracker::class.java, "data1").build())
            .build()

        val mediaItem2 = mediaItem.buildUpon()
            .setTrackerData(mediaItem.getMediaItemTrackerData().buildUpon().putData(Tracker::class.java, "data1").build())
            .build()
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual same MediaItemTrackerData but different MediaMetadata`() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTrackerData(MediaItemTrackerData.Builder().putData(Tracker::class.java, "data1").build())
            .build()

        val mediaItem2 = mediaItem.buildUpon()
            .setTrackerData(mediaItem.getMediaItemTrackerData().buildUpon().putData(Tracker::class.java, "data1").build())
            .setMediaMetadata(MediaMetadata.Builder().setTitle("New title").build())
            .build()
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    @Test
    fun `areEqual different data`() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://streaming.com/video.mp4")
            .setTrackerData(MediaItemTrackerData.Builder().putData(Tracker::class.java, "data1").build())
            .build()

        val mediaItem2 = mediaItem.buildUpon()
            .setTrackerData(mediaItem.getMediaItemTrackerData().buildUpon().putData(Tracker::class.java, "data2").build())
            .build()
        assertTrue(AnalyticsMediaItemTracker.areEqual(mediaItem, mediaItem2))
    }

    private class Tracker : MediaItemTracker {
        override fun start(player: ExoPlayer, initialData: Any?) {
            // Nothing
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
            // Nothing
        }
    }

    companion object {
        private fun createMediaItemWithMediaId(
            mediaId: String,
            url: String? = null,
            tag: Any? = null,
        ): MediaItem {
            return MediaItem.Builder()
                .setUri(url)
                .setMediaId(mediaId)
                .setTag(tag)
                .build()
        }
    }
}
