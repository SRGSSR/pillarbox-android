/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.test.utils.FakeClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class PillarboxPlayerMediaItemTest {
    private lateinit var player: PillarboxPlayer

    @Before
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        player = PillarboxPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = FakeClock(true),
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `set MediaItem without uri throw error`() {
        val mediaItem = MediaItem.Builder()
            .setMediaId("MediaID")
            .build()
        player.setMediaItem(mediaItem)
    }

    @Test
    fun `set MediaItem clear tag`() {
        val mediaItem = MEDIA_1_WITH_TAG
        val expectedMediaItem = mediaItem.buildUpon().setTag(null).build()

        player.setMediaItem(mediaItem)
        assertEquals(expectedMediaItem, player.currentMediaItem)

        player.setMediaItem(mediaItem, false)
        assertEquals(expectedMediaItem, player.currentMediaItem)

        player.setMediaItem(mediaItem, 10L)
        assertEquals(expectedMediaItem, player.currentMediaItem)
    }

    @Test
    fun `add MediaItem clear tag`() {
        val mediaItem = MEDIA_1_WITH_TAG
        val expectedMediaItem = mediaItem.buildUpon().setTag(null).build()

        player.addMediaItem(mediaItem)
        assertEquals(expectedMediaItem, player.currentMediaItem)

        player.clearMediaItems()

        player.addMediaItem(0, mediaItem)
        assertEquals(expectedMediaItem, player.currentMediaItem)
    }

    @Test
    fun `replace MediaItem clear tag`() {
        val mediaItem = MEDIA_1_WITH_TAG
        val expectedMediaItem = mediaItem.buildUpon().setTag(null).build()

        player.setMediaItem(mediaItem)
        assertEquals(expectedMediaItem, player.currentMediaItem)

        val expectedMediaItem2 = MEDIA_2_WITH_TAG.buildUpon().setTag(null).build()
        player.replaceMediaItem(0, MEDIA_2_WITH_TAG)
        assertEquals(expectedMediaItem2, player.currentMediaItem)
    }

    @Test
    fun `set MediaItems clear tag`() {
        val mediaItems = listOf(MEDIA_1_WITH_TAG, MEDIA_2_WITH_TAG, MEDIA_3_WITHOUT_TAG)
        val expectedMediaItems = listOf(
            MEDIA_1_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_2_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_3_WITHOUT_TAG.buildUpon().setTag(null).build(),
        )
        player.setMediaItems(mediaItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())

        player.setMediaItems(mediaItems, false)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())

        player.setMediaItems(mediaItems, 0, 10L)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `add MediaItems clear tag`() {
        val mediaItems = listOf(MEDIA_1_WITH_TAG, MEDIA_2_WITH_TAG, MEDIA_3_WITHOUT_TAG)
        val expectedMediaItems = listOf(
            MEDIA_1_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_2_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_3_WITHOUT_TAG.buildUpon().setTag(null).build(),
        )
        player.addMediaItems(mediaItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())

        player.clearMediaItems()
        player.addMediaItems(0, mediaItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    @Test
    fun `replace MediaItems clear tag`() {
        val mediaItems = listOf(MEDIA_1_WITH_TAG, MEDIA_2_WITH_TAG, MEDIA_3_WITHOUT_TAG)
        val expectedMediaItems = listOf(
            MEDIA_1_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_2_WITH_TAG.buildUpon().setTag(null).build(),
            MEDIA_3_WITHOUT_TAG.buildUpon().setTag(null).build(),
        )
        player.setMediaItems(mediaItems)
        player.replaceMediaItems(0, mediaItems.size, mediaItems)
        assertEquals(expectedMediaItems, player.getCurrentMediaItems())
    }

    companion object {
        private val MEDIA_1_WITH_TAG = MediaItem.Builder()
            .setUri("uri1")
            .setTag("TAG1")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title 1")
                    .build()
            )
            .build()
        private val MEDIA_2_WITH_TAG = MediaItem.Builder()
            .setMediaId("MediaId_2")
            .setUri("uri2")
            .setTag("TAG2")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title 2")
                    .build()
            )
            .build()
        private val MEDIA_3_WITHOUT_TAG = MediaItem.Builder()
            .setMediaId("MediaId_3")
            .setUri("uri3")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title 3")
                    .build()
            )
            .build()
    }
}
