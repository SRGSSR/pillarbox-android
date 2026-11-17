/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
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
    fun `encode and decode trackers data`() {
        val customData = byteArrayOf(3, 4)
        val trackersData = "trackers".encodeToByteArray()
        val data = DownloadData.decode(DownloadData("Title", customData, trackersData).encode())

        assertNotNull(data)
        assertEquals("Title", data.title)
        assertContentEquals(customData, data.customData)
        assertContentEquals(trackersData, data.trackersData)
    }

    @Test
    fun `encode and decode without trackers data`() {
        val data = DownloadData.decode(DownloadData("Title", null).encode())

        assertNotNull(data)
        assertNull(data.trackersData)
    }

    @Test
    fun `decode version 1`() {
        val customData = byteArrayOf(1, 2, 3)
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeByte(1)
            output.writeBoolean(true)
            output.writeUTF("Title")
            output.writeInt(customData.size)
            output.write(customData)
        }
        val data = DownloadData.decode(bytes.toByteArray())

        assertNotNull(data)
        assertEquals("Title", data.title)
        assertContentEquals(customData, data.customData)
        assertNull(data.trackersData)
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
        val encoded = DownloadData("Title", byteArrayOf(1, 2, 3), byteArrayOf(4, 5)).encode()

        assertNull(DownloadData.decode(encoded.copyOf(encoded.size - 1)))
    }

    @Test
    fun `decode data not encoded by DownloadData`() {
        assertNull(DownloadData.decode("Title".encodeToByteArray()))
    }
}
