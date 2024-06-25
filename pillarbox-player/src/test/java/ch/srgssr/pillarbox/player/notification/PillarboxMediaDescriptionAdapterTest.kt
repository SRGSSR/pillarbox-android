/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.notification

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class PillarboxMediaDescriptionAdapterTest {
    private lateinit var pendingIntent: PendingIntent
    private lateinit var mediaDescriptionAdapter: MediaDescriptionAdapter

    @BeforeTest
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        pendingIntent = mockk()
        mediaDescriptionAdapter = PillarboxMediaDescriptionAdapter(pendingIntent, context)
    }

    @Test
    fun `get current content title, with displayTitle and title in metadata`() {
        val displayTitle = "Media display title"
        val title = "Media title"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setDisplayTitle(displayTitle)
                .setTitle(title)
                .build()
        }

        assertEquals(displayTitle, mediaDescriptionAdapter.getCurrentContentTitle(player))
    }

    @Test
    fun `get current content title, with displayTitle empty and title in metadata`() {
        val displayTitle = ""
        val title = "Media title"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setDisplayTitle(displayTitle)
                .setTitle(title)
                .build()
        }

        assertEquals(title, mediaDescriptionAdapter.getCurrentContentTitle(player))
    }

    @Test
    fun `get current content title, with title only in metadata`() {
        val title = "Media title"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setTitle(title)
                .build()
        }

        assertEquals(title, mediaDescriptionAdapter.getCurrentContentTitle(player))
    }

    @Test
    fun `get current content title, with no titles`() {
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder().build()
        }

        assertEquals("", mediaDescriptionAdapter.getCurrentContentTitle(player))
    }

    @Test
    fun `create current content intent`() {
        assertEquals(pendingIntent, mediaDescriptionAdapter.createCurrentContentIntent(mockk()))
    }

    @Test
    fun `get current content text, with subtitle and station in metadata`() {
        val subtitle = "Media subtitle"
        val station = "Media station"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setSubtitle(subtitle)
                .setStation(station)
                .build()
        }

        assertEquals(subtitle, mediaDescriptionAdapter.getCurrentContentText(player))
    }

    @Test
    fun `get current content text, with subtitle empty and station in metadata`() {
        val subtitle = ""
        val station = "Media station"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setSubtitle(subtitle)
                .setStation(station)
                .build()
        }

        assertEquals(station, mediaDescriptionAdapter.getCurrentContentText(player))
    }

    @Test
    fun `get current content title, with station only in metadata`() {
        val station = "Media station"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setStation(station)
                .build()
        }

        assertEquals(station, mediaDescriptionAdapter.getCurrentContentText(player))
    }

    @Test
    fun `get current content title, with no subtitle nor station`() {
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder().build()
        }

        assertNull(mediaDescriptionAdapter.getCurrentContentText(player))
    }

    @Test
    fun `get current large icon, no artworkData nor artworkUri`() {
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder().build()
        }

        assertNull(mediaDescriptionAdapter.getCurrentLargeIcon(player, mockk()))
    }

    @Test
    fun `get current large icon, with artworkData only`() {
        val artworkData = byteArrayOf(35, 12, 6, 77)
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FILE_ICON)
                .build()
        }
        val bitmap = mediaDescriptionAdapter.getCurrentLargeIcon(player, mockk())
        val shadowBitmap = shadowOf(bitmap)

        assertNotNull(bitmap)
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)

        assertContentEquals(artworkData, shadowBitmap.createdFromBytes)
    }

    @Test
    fun `get current large icon, with bad artworkUri only`() {
        val artworkUri = "https://www.example.com/my/image.png"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setArtworkUri(Uri.parse(artworkUri))
                .build()
        }

        assertNull(mediaDescriptionAdapter.getCurrentLargeIcon(player, mockk()))
    }

    @Test
    fun `get current large icon, with both artworkData and artworkUri`() {
        val artworkData = byteArrayOf(35, 12, 6, 77)
        val artworkUri = "https://source.android.com/static/docs/setup/images/Android_symbol_green_RGB.png"
        val player = mockk<Player> {
            every { mediaMetadata } returns MediaMetadata.Builder()
                .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FILE_ICON)
                .setArtworkUri(Uri.parse(artworkUri))
                .build()
        }
        val bitmap = mediaDescriptionAdapter.getCurrentLargeIcon(player, mockk())
        val shadowBitmap = shadowOf(bitmap)

        assertNotNull(bitmap)
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)

        assertContentEquals(artworkData, shadowBitmap.createdFromBytes)
    }
}
