/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.runner.RunWith
import java.util.concurrent.ExecutionException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DefaultMediaSessionCallbackTest {
    private lateinit var mediaSessionCallback: MediaSession.Callback

    @BeforeTest
    fun setup() {
        mediaSessionCallback = object : DefaultMediaSessionCallback {}
    }

    @Test(expected = ExecutionException::class)
    fun `onAddMediaItems with missing localConfiguration`() {
        val mediaItems = listOf(
            MediaItem.Builder().setUri("https://host/media.mp4").build(),
            MediaItem.Builder().build(),
        )
        val future = mediaSessionCallback.onAddMediaItems(mockk(), mockk(), mediaItems)

        assertTrue(future.isDone)

        future.get()
    }

    @Test(expected = ExecutionException::class)
    fun `onAddMediaItems with empty mediaId`() {
        val mediaItems = listOf(
            MediaItem.Builder().setUri("https://host/media.mp4").build(),
            MediaItem.Builder().setMediaId("").build(),
        )
        val future = mediaSessionCallback.onAddMediaItems(mockk(), mockk(), mediaItems)

        assertTrue(future.isDone)

        future.get()
    }

    @Test(expected = ExecutionException::class)
    fun `onAddMediaItems with blank mediaId`() {
        val mediaItems = listOf(
            MediaItem.Builder().setUri("https://host/media.mp4").build(),
            MediaItem.Builder().setMediaId(" ").build(),
        )
        val future = mediaSessionCallback.onAddMediaItems(mockk(), mockk(), mediaItems)

        assertTrue(future.isDone)

        future.get()
    }

    @Test
    fun `onAddMediaItems with valid MediaItem`() {
        val mediaItems = listOf(
            MediaItem.Builder().setUri("https://host/media1.mp4").build(),
            MediaItem.Builder().setUri("https://host/media2.mp4").build(),
        )
        val future = mediaSessionCallback.onAddMediaItems(mockk(), mockk(), mediaItems)

        assertTrue(future.isDone)

        val result = future.get()

        assertEquals(mediaItems, result)
    }
}
