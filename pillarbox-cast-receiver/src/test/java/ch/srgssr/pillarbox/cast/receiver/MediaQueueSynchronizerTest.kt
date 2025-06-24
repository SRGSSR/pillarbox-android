/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.content.Context
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import io.mockk.unmockkAll
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MediaQueueSynchronizerTest {

    private lateinit var player: Player
    private lateinit var mediaQueueSynchronizer: MediaQueueSynchronizer

    @BeforeTest
    fun init() {
        var itemId = 0
        val context: Context = ApplicationProvider.getApplicationContext()
        player = ExoPlayer.Builder(context).build()
        mediaQueueSynchronizer = MediaQueueSynchronizer(
            player = player,
            mediaItemConverter = TestItemConverter,
            autoGenerateItemId = { itemId++ },
        )
    }

    @AfterTest
    fun release() {
        player.clearMediaItems()
        player.release()
        unmockkAll()
    }

    @Test
    fun `notify items`() {
        val mediaItems = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
        )
        val expectedMediaQueueItems = listOf(
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
        )
        mediaQueueSynchronizer.notifySetMediaItems(mediaItems)
        assertEquals(expectedMediaQueueItems, mediaQueueSynchronizer.mediaQueueItems)
    }

    @Test
    fun `add media items`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val mediaItems = listOf(
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
        )
        val expectedItems = listOf(
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
        )
        val expectedMediaItems = listOf(
            initialPlaylist[0],
            mediaItems[0],
            mediaItems[1],
            initialPlaylist[1]
        )
        mediaQueueSynchronizer.addMediaItems(1, mediaItems)

        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `remove media items`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val expectedItems = listOf(
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
        )
        val expectedMediaItems = listOf(
            initialPlaylist[2],
            initialPlaylist[3]
        )
        mediaQueueSynchronizer.removeMediaItems(0, 2)

        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
    }

    @Test
    fun `move media items`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
            createMediaItem("5", "https://url5.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val expectedItems = listOf(
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
            createMediaQueueItem("5", 4, "https://url5.m3u8"),
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
        )
        val expectedMediaItems = listOf(
            initialPlaylist[0],
            initialPlaylist[3],
            initialPlaylist[4],
            initialPlaylist[1],
            initialPlaylist[2],
        )
        mediaQueueSynchronizer.moveMediaItems(1, toIndex = 3, 3)

        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
    }

    @Test
    fun `queue insert before id`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val queueItem = listOf(
            createMediaQueueItem("3", -1, "https://url3.m3u8"),
            createMediaQueueItem("4", -1, "https://url4.m3u8"),
        )
        val expectedItems = listOf(
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
        )
        val expectedMediaItems = expectedItems.map { TestItemConverter.toMediaItem(it) }

        mediaQueueSynchronizer.queueInsert(queueItem, 1)

        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `queue insert ad the end of the playlist`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),

        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val queueItem = listOf(
            createMediaQueueItem("3", -1, "https://url3.m3u8"),
            createMediaQueueItem("4", -1, "https://url4.m3u8"),
        )
        val expectedItems = listOf(
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
        )
        val expectedMediaItems = expectedItems.map { TestItemConverter.toMediaItem(it) }

        mediaQueueSynchronizer.queueInsert(queueItem, null)

        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `queue reorder insert before id`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
            createMediaItem("5", "https://url5.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val expectedItems = listOf(
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("5", 5, "https://url5.m3u8"),
        )
        val expectedMediaItems = expectedItems.map { TestItemConverter.toMediaItem(it) }

        val itemIdToRemove = listOf(0, 3)
        mediaQueueSynchronizer.queueReorder(itemIdToRemove, 2)

        assertEquals(expectedItems.map { it.media?.contentId }, mediaQueueSynchronizer.mediaQueueItems.map { it.media?.contentId })
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `queue reorder insert at the end of the playlist`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
            createMediaItem("5", "https://url5.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val expectedItems = listOf(
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
            createMediaQueueItem("5", 5, "https://url5.m3u8"),
            createMediaQueueItem("1", 0, "https://url1.m3u8"),
            createMediaQueueItem("4", 3, "https://url4.m3u8"),
        )
        val expectedMediaItems = expectedItems.map { TestItemConverter.toMediaItem(it) }

        val itemIdToRemove = listOf(0, 3)
        mediaQueueSynchronizer.queueReorder(itemIdToRemove, null)

        assertEquals(expectedItems.map { it.media?.contentId }, mediaQueueSynchronizer.mediaQueueItems.map { it.media?.contentId })
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `remove queue media items`() {
        val initialPlaylist = listOf(
            createMediaItem("1", "https://url1.m3u8"),
            createMediaItem("2", "https://url2.m3u8"),
            createMediaItem("3", "https://url3.m3u8"),
            createMediaItem("4", "https://url4.m3u8"),
        )
        player.setMediaItems(initialPlaylist)
        mediaQueueSynchronizer.notifySetMediaItems(initialPlaylist)

        val expectedItems = listOf(
            createMediaQueueItem("2", 1, "https://url2.m3u8"),
            createMediaQueueItem("3", 2, "https://url3.m3u8"),
        )
        val expectedMediaItems = expectedItems.map { TestItemConverter.toMediaItem(it) }
        val idToRemove = listOf(0, 3)
        val removedIds = mediaQueueSynchronizer.removeQueueItems(idToRemove)

        assertEquals(idToRemove, removedIds)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
        assertEquals(expectedItems, mediaQueueSynchronizer.mediaQueueItems)
    }

    companion object {
        fun createMediaItem(id: String, url: String) = MediaItem.Builder().setMediaId(id).setUri(url).build()

        fun createMediaQueueItem(contentId: String, id: Int, url: String): MediaQueueItem {
            val mediaInfo = MediaInfo.Builder(contentId)
                .setContentUrl(url)
                .build()
            return MediaQueueItem.Builder(mediaInfo)
                .setItemId(id)
                .build()
        }
    }

    private object TestItemConverter : MediaItemConverter {
        override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
            val mediaInfo = MediaInfo.Builder(mediaItem.mediaId)
                .setContentUrl(checkNotNull(mediaItem.localConfiguration).uri.toString())
                .build()
            return MediaQueueItem.Builder(mediaInfo)
                .build()
        }

        override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
            return MediaItem.Builder()
                .setUri(mediaQueueItem.media?.contentUrl)
                .setMediaId(mediaQueueItem.media?.contentId ?: MediaItem.DEFAULT_MEDIA_ID)
                .build()
        }
    }
}
