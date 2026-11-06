/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import android.net.Uri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DownloadExtensionsTest {
    @Test
    fun `title and custom data`() {
        val customData = "https://image.url".encodeToByteArray()
        val download = createDownload(data = DownloadData("Title", customData).encode())

        assertEquals("Title", download.title)
        assertContentEquals(customData, download.customData)
    }

    @Test
    fun `title and custom data without data`() {
        val download = createDownload(data = null)

        assertNull(download.title)
        assertNull(download.customData)
    }

    @Test
    fun `is paused`() {
        val download = createDownload(state = Download.STATE_STOPPED, stopReason = PillarboxDownloadManager.STOP_REASON_PAUSED)

        assertTrue(download.isPaused)
    }

    @Test
    fun `is not paused when stopped for another reason`() {
        val download = createDownload(state = Download.STATE_STOPPED, stopReason = 2)

        assertFalse(download.isPaused)
    }

    @Test
    fun `is not paused when not stopped`() {
        val download = createDownload(state = Download.STATE_DOWNLOADING)

        assertFalse(download.isPaused)
    }

    private fun createDownload(
        data: ByteArray? = null,
        state: Int = Download.STATE_COMPLETED,
        stopReason: Int = Download.STOP_REASON_NONE,
    ): Download {
        val request = DownloadRequest.Builder("id", Uri.parse("https://example.com/media.m3u8"))
            .setData(data)
            .build()

        return Download(request, state, 0L, 0L, 0L, stopReason, Download.FAILURE_REASON_NONE)
    }
}
