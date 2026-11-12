/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import androidx.media3.common.C
import androidx.media3.common.DrmInitData
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

@RunWith(AndroidJUnit4::class)
class OfflineDrmSessionManagerTest {
    @Test
    fun `no license`() {
        assertFailsWith<IllegalArgumentException> { OfflineDrmSessionManager(emptyList()) }
    }

    @Test
    fun `tracks are decrypted with the license of their PSSH box`() {
        val manager = OfflineDrmSessionManager(listOf(VIDEO_LICENSE, AUDIO_LICENSE))

        assertEquals(0, manager.getLicenseIndex(format(VIDEO_PSSH)))
        assertEquals(1, manager.getLicenseIndex(format(AUDIO_PSSH)))
    }

    @Test
    fun `the first license is used for tracks without matching PSSH box`() {
        val manager = OfflineDrmSessionManager(listOf(VIDEO_LICENSE, AUDIO_LICENSE))

        assertEquals(0, manager.getLicenseIndex(format(byteArrayOf(9))))
        assertEquals(0, manager.getLicenseIndex(Format.Builder().build()))
        assertEquals(0, manager.getLicenseIndex(format(AUDIO_PSSH, uuid = C.PLAYREADY_UUID)))
    }

    @Test
    fun `a license without PSSH box is used for all the tracks`() {
        val manager = OfflineDrmSessionManager(listOf(OfflineLicense(psshData = null, keySetId = byteArrayOf(1))))

        assertEquals(0, manager.getLicenseIndex(format(VIDEO_PSSH)))
        assertEquals(0, manager.getLicenseIndex(format(AUDIO_PSSH)))
    }

    @Test
    fun `provider uses the offline licenses in the media item tag`() {
        val delegateManager = DrmSessionManager.DRM_UNSUPPORTED
        val provider = OfflineDrmSessionManagerProvider { delegateManager }
        val downloadedMediaItem = MediaItem.Builder()
            .setUri("https://example.com/media.mpd")
            .setTag(OfflineLicenses(listOf(VIDEO_LICENSE)))
            .build()
        val otherMediaItem = MediaItem.Builder()
            .setUri("https://example.com/media.mpd")
            .build()

        assertIs<OfflineDrmSessionManager>(provider.get(downloadedMediaItem))
        assertSame(delegateManager, provider.get(otherMediaItem))
    }

    private companion object {
        private val VIDEO_PSSH = byteArrayOf(1, 2, 3)
        private val AUDIO_PSSH = byteArrayOf(4, 5, 6)
        private val VIDEO_LICENSE = OfflineLicense(psshData = VIDEO_PSSH, keySetId = byteArrayOf(10))
        private val AUDIO_LICENSE = OfflineLicense(psshData = AUDIO_PSSH, keySetId = byteArrayOf(11))

        private fun format(psshData: ByteArray, uuid: java.util.UUID = C.WIDEVINE_UUID): Format {
            return Format.Builder()
                .setDrmInitData(DrmInitData(DrmInitData.SchemeData(uuid, MimeTypes.VIDEO_MP4, psshData)))
                .build()
        }
    }
}
