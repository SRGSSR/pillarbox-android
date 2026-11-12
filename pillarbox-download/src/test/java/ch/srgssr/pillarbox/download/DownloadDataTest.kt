/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import ch.srgssr.pillarbox.player.offline.OfflineLicense
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DownloadDataTest {
    @Test
    fun `encode and decode title and custom data`() {
        val customData = byteArrayOf(0, 1, 2, -1)
        val data = DownloadData.decode(DownloadData("Title", customData).encode())

        assertNotNull(data)
        assertEquals("Title", data.title)
        assertContentEquals(customData, data.customData)
    }

    @Test
    fun `encode and decode without title`() {
        val customData = "data".encodeToByteArray()
        val data = DownloadData.decode(DownloadData(null, customData).encode())

        assertNotNull(data)
        assertNull(data.title)
        assertContentEquals(customData, data.customData)
    }

    @Test
    fun `encode and decode without custom data`() {
        val data = DownloadData.decode(DownloadData("Title", null).encode())

        assertNotNull(data)
        assertEquals("Title", data.title)
        assertNull(data.customData)
    }

    @Test
    fun `encode and decode empty values`() {
        val data = DownloadData.decode(DownloadData("", ByteArray(0)).encode())

        assertNotNull(data)
        assertEquals("", data.title)
        assertContentEquals(ByteArray(0), data.customData)
    }

    @Test
    fun `encode and decode non ASCII title`() {
        val title = "Émission spéciale 🎬"
        val data = DownloadData.decode(DownloadData(title, null).encode())

        assertEquals(title, data?.title)
    }

    @Test
    fun `encode and decode offline licenses`() {
        val licenses = listOf(
            OfflineLicense(psshData = byteArrayOf(1, 2), keySetId = byteArrayOf(3)),
            OfflineLicense(psshData = null, keySetId = byteArrayOf(4, 5)),
        )
        val data = assertNotNull(DownloadData.decode(DownloadData("Title", null, licenses).encode()))

        assertEquals("Title", data.title)
        assertEquals(licenses, data.offlineLicenses)
    }

    @Test
    fun `decode data of version 1`() {
        // Version 1, with the title "T" and without custom data
        val encoded = byteArrayOf(1, 1, 0, 1, 'T'.code.toByte(), -1, -1, -1, -1)
        val data = assertNotNull(DownloadData.decode(encoded))

        assertEquals("T", data.title)
        assertNull(data.customData)
        assertEquals(emptyList(), data.offlineLicenses)
    }

    @Test
    fun `decode empty data`() {
        assertNull(DownloadData.decode(ByteArray(0)))
    }

    @Test
    fun `decode unknown version`() {
        val encoded = DownloadData("Title", null).encode()
        encoded[0] = 3

        assertNull(DownloadData.decode(encoded))
    }

    @Test
    fun `decode truncated data`() {
        val encoded = DownloadData("Title", byteArrayOf(1, 2, 3)).encode()

        assertNull(DownloadData.decode(encoded.copyOf(encoded.size - 1)))
    }

    @Test
    fun `decode data not encoded by DownloadData`() {
        assertNull(DownloadData.decode("Title".encodeToByteArray()))
    }
}
