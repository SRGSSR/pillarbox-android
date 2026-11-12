/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.player.offline.OfflineLicense
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * The title of the downloaded media, as stored by [PillarboxDownloadManager.download], or `null` if unknown.
 */
val Download.title: String?
    get() = DownloadData.decode(request.data)?.title

/**
 * The application specific data passed to [PillarboxDownloadManager.download], or `null` if none.
 */
val Download.customData: ByteArray?
    get() = DownloadData.decode(request.data)?.customData

/**
 * Whether the download has been paused with [PillarboxDownloadManager.pause].
 */
val Download.isPaused: Boolean
    get() = state == Download.STATE_STOPPED && stopReason == PillarboxDownloadManager.STOP_REASON_PAUSED

/**
 * The data stored by [PillarboxDownloadManager] in [DownloadRequest.data][androidx.media3.exoplayer.offline.DownloadRequest.data]. It starts
 * with a version, so that its format can evolve.
 *
 * @property title The title of the media, or `null`.
 * @property customData The application specific data, or `null`.
 * @property offlineLicenses The Widevine offline licenses of the media, empty if it is not DRM protected.
 */
internal class DownloadData(
    val title: String?,
    val customData: ByteArray?,
    val offlineLicenses: List<OfflineLicense> = emptyList(),
) {
    fun encode(): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeByte(VERSION)
            output.writeBoolean(title != null)
            title?.let(output::writeUTF)
            output.writeByteArray(customData)
            output.writeInt(offlineLicenses.size)
            offlineLicenses.forEach { license ->
                output.writeByteArray(license.psshData)
                output.writeByteArray(license.keySetId)
            }
        }
        return bytes.toByteArray()
    }

    companion object {
        private const val VERSION_WITHOUT_LICENSES = 1
        private const val VERSION = 2
        private const val NULL_BYTE_ARRAY = -1

        /**
         * Decodes the data encoded with [encode], or returns `null` if it is empty or invalid.
         */
        fun decode(data: ByteArray): DownloadData? {
            if (data.isEmpty()) return null
            return try {
                DataInputStream(ByteArrayInputStream(data)).use(::read)
            } catch (_: IOException) {
                null
            }
        }

        private fun read(input: DataInputStream): DownloadData? {
            val version = input.readByte().toInt()
            if (version != VERSION && version != VERSION_WITHOUT_LICENSES) return null
            val title = if (input.readBoolean()) input.readUTF() else null
            val customData = input.readByteArray()
            val offlineLicenses = if (version == VERSION) {
                List(input.readInt()) {
                    val psshData = input.readByteArray()
                    val keySetId = input.readByteArray() ?: throw IOException("Missing key set id")
                    OfflineLicense(psshData, keySetId)
                }
            } else {
                emptyList()
            }
            return DownloadData(title, customData, offlineLicenses)
        }

        private fun DataOutputStream.writeByteArray(bytes: ByteArray?) {
            writeInt(bytes?.size ?: NULL_BYTE_ARRAY)
            bytes?.let(::write)
        }

        private fun DataInputStream.readByteArray(): ByteArray? {
            val size = readInt()
            return if (size < 0) null else ByteArray(size).also(::readFully)
        }
    }
}
