/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class DownloadedMediaAssetLoaderTest {
    private val provider = object : DownloadedMediaProvider {
        override val cache get() = error("Not used")

        override fun getDownloadRequest(mediaItem: MediaItem): DownloadRequest? = null
    }

    @Test
    fun `media item of a download without offline license`() {
        val downloadRequest = DownloadRequest.Builder(MEDIA_ID, Uri.parse(URI)).build()

        val mediaItem = downloadRequest.toMediaItem(createMediaItemBuilder(provider.getOfflineLicenses(downloadRequest)))

        assertEquals(MEDIA_ID, mediaItem.mediaId)
        assertNull(mediaItem.localConfiguration?.drmConfiguration)
    }

    @Test
    fun `media item of a download with an offline license`() {
        val keySetId = byteArrayOf(1, 2, 3)
        val downloadRequest = DownloadRequest.Builder(MEDIA_ID, Uri.parse(URI))
            .setKeySetId(keySetId)
            .build()

        val mediaItem = downloadRequest.toMediaItem(createMediaItemBuilder(provider.getOfflineLicenses(downloadRequest)))
        val drmConfiguration = assertNotNull(mediaItem.localConfiguration?.drmConfiguration)

        assertEquals(MEDIA_ID, mediaItem.mediaId)
        assertEquals(Uri.parse(URI), mediaItem.localConfiguration?.uri)
        assertEquals(C.WIDEVINE_UUID, drmConfiguration.scheme)
        assertContentEquals(keySetId, drmConfiguration.keySetId)
        assertEquals(listOf(OfflineLicense(null, keySetId)), (mediaItem.localConfiguration?.tag as OfflineLicenses).licenses)
    }

    private companion object {
        private const val MEDIA_ID = "urn:rts:video:1234"
        private const val URI = "https://example.com/media.mpd"
    }
}
