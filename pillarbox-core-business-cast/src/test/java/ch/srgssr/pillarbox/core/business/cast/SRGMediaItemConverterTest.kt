/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.cast

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SRGMediaItemConverterTest {

    @Test
    fun `verify SRGMediaItemConverter convert loop`() {
        val mediaItem = MediaItem.Builder()
            .setMediaId("id1")
            .setUri("https://www.media.ch/stream.mpd")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title")
                    .setSubtitle("Subtitle")
                    .setArtworkUri(Uri.parse("https://www.media.ch/artwork.jpg"))
                    .build()
            )
            .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).setLicenseUri("https://license.ch").build())
            .build()
        val converter = SRGMediaItemConverter()
        val mediaItemOutput = converter.toMediaItem(converter.toMediaQueueItem(mediaItem))
        assertEquals(mediaItem, mediaItemOutput)
    }

    @Test
    fun `verify urn with host`() {
        val mediaItem = SRGMediaItem(urn = "urn:rts:video:1234") {
            host(IlHost.STAGE)
        }
        val converter = SRGMediaItemConverter()
        val mediaItemOutput = converter.toMediaItem(converter.toMediaQueueItem(mediaItem))
        assertEquals(mediaItem, mediaItemOutput)
    }
}
