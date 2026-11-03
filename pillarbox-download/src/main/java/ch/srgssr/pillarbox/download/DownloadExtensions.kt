/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import androidx.media3.exoplayer.offline.Download
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
 */
internal class DownloadData(val title: String?, val customData: ByteArray?) {
    fun encode(): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeByte(VERSION)
            output.writeBoolean(title != null)
            title?.let(output::writeUTF)
            output.writeInt(customData?.size ?: NO_CUSTOM_DATA)
            customData?.let(output::write)
        }
        return bytes.toByteArray()
    }

    companion object {
        private const val VERSION = 1
        private const val NO_CUSTOM_DATA = -1

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
            if (input.readByte().toInt() != VERSION) return null
            val title = if (input.readBoolean()) input.readUTF() else null
            val customDataSize = input.readInt()
            val customData = if (customDataSize < 0) null else ByteArray(customDataSize).also(input::readFully)
            return DownloadData(title, customData)
        }
    }
}
