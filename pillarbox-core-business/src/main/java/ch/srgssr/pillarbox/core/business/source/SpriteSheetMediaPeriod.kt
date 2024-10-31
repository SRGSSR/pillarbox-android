/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.exoplayer.FormatHolder
import androidx.media3.exoplayer.LoadingInfo
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.SampleStream
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * Sprite sheet media period
 */
internal class SpriteSheetMediaPeriod(private val spriteSheet: SpriteSheet) : MediaPeriod {
    private lateinit var bitmap: Bitmap
    private val isLoading = AtomicBoolean(true)
    private val format = spriteSheet.let {
        Format.Builder()
            .setId(it.urn)
            .setFrameRate(1f / it.interval.milliseconds.inWholeSeconds)
            .setCustomData(it)
            .setRoleFlags(C.ROLE_FLAG_MAIN)
            .setContainerMimeType(MimeTypes.IMAGE_JPEG)
            .setSampleMimeType(MimeTypes.IMAGE_JPEG)
            .build()
    }
    private val tracks = TrackGroupArray((TrackGroup("sprite-sheet-srg", format)))
    private var positionUs = 0L

    override fun prepare(callback: MediaPeriod.Callback, positionUs: Long) {
        this.positionUs = positionUs
        callback.onPrepared(this)
        URL(spriteSheet.url).openStream().use {
            isLoading.set(true)
            bitmap = BitmapFactory.decodeStream(it)
            isLoading.set(false)
        }
    }

    fun releasePeriod() {
        bitmap.recycle()
    }

    override fun selectTracks(
        selections: Array<out ExoTrackSelection?>,
        mayRetainStreamFlags: BooleanArray,
        streams: Array<SampleStream?>,
        streamResetFlags: BooleanArray,
        positionUs: Long
    ): Long {
        this.positionUs = positionUs
        for (i in selections.indices) {
            if (streams[i] != null && (selections[i] == null || !mayRetainStreamFlags[i])) {
                streams[i] = null
            }
            if (streams[i] == null && selections[i] != null) {
                val stream = SpriteSheetSampleStream()
                streams[i] = stream
                streamResetFlags[i] = true
            }
        }
        return positionUs
    }

    override fun getTrackGroups(): TrackGroupArray {
        return tracks
    }

    override fun getBufferedPositionUs(): Long {
        return C.TIME_END_OF_SOURCE
    }

    override fun getNextLoadPositionUs(): Long {
        return C.TIME_END_OF_SOURCE
    }

    override fun continueLoading(loadingInfo: LoadingInfo): Boolean {
        return isLoading.get()
    }

    override fun isLoading(): Boolean {
        return isLoading.get()
    }

    override fun reevaluateBuffer(positionUs: Long) = Unit

    override fun maybeThrowPrepareError() = Unit

    override fun discardBuffer(positionUs: Long, toKeyframe: Boolean) = Unit

    override fun readDiscontinuity(): Long {
        return C.TIME_UNSET
    }

    override fun seekToUs(positionUs: Long): Long {
        this.positionUs = positionUs
        return positionUs
    }

    override fun getAdjustedSeekPositionUs(positionUs: Long, seekParameters: SeekParameters): Long {
        val intervalUs = spriteSheet.interval.milliseconds.inWholeMicroseconds
        return (positionUs / intervalUs) * intervalUs
    }

    internal inner class SpriteSheetSampleStream : SampleStream {
        private var streamState = STREAM_STATE_SEND_FORMAT

        override fun isReady(): Boolean {
            return !isLoading()
        }

        override fun maybeThrowError() = Unit

        @Suppress("ReturnCount")
        override fun readData(formatHolder: FormatHolder, buffer: DecoderInputBuffer, readFlags: Int): Int {
            if (streamState == STREAM_STATE_END_OF_STREAM) {
                buffer.addFlag(C.BUFFER_FLAG_END_OF_STREAM)
                return C.RESULT_BUFFER_READ
            }

            if ((readFlags and SampleStream.FLAG_REQUIRE_FORMAT) != 0 || streamState == STREAM_STATE_SEND_FORMAT) {
                formatHolder.format = tracks[0].getFormat(0)
                streamState = STREAM_STATE_SEND_SAMPLE
                return C.RESULT_FORMAT_READ
            }
            val intervalUs = spriteSheet.interval.milliseconds.inWholeMicroseconds
            val tileIndex = positionUs / intervalUs
            buffer.addFlag(C.BUFFER_FLAG_KEY_FRAME)
            buffer.timeUs = positionUs
            if ((readFlags and SampleStream.FLAG_OMIT_SAMPLE_DATA) == 0) {
                val data = cropTileFromImageGrid(max((tileIndex.toInt() - 1), 0))
                buffer.ensureSpaceForWrite(data.size)
                buffer.data!!.put(data, /* offset= */0, data.size)
            }
            if ((readFlags and SampleStream.FLAG_PEEK) == 0 && tileIndex >= (spriteSheet.rows * spriteSheet.columns) - 1) {
                streamState = STREAM_STATE_END_OF_STREAM
            }
            return C.RESULT_BUFFER_READ
        }

        override fun skipData(positionUs: Long): Int {
            return 0
        }

        private fun cropTileFromImageGrid(tileIndex: Int): ByteArray {
            val tileWidth: Int = spriteSheet.thumbnailWidth
            val tileHeight: Int = spriteSheet.thumbnailHeight
            val tileStartXCoordinate: Int = tileWidth * (tileIndex % spriteSheet.columns)
            val tileStartYCoordinate: Int = tileHeight * (tileIndex / spriteSheet.columns)
            val tile = Bitmap.createBitmap(bitmap, tileStartXCoordinate, tileStartYCoordinate, tileWidth, tileHeight)
            return bitmapToByteArray(tile, Bitmap.CompressFormat.JPEG, MAX_QUALITY)
        }
    }

    private companion object {
        private const val STREAM_STATE_SEND_FORMAT: Int = 0
        private const val STREAM_STATE_SEND_SAMPLE: Int = 1
        private const val STREAM_STATE_END_OF_STREAM: Int = 2
        private const val MAX_QUALITY = 100

        fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(format, quality, stream)
            return stream.toByteArray()
        }
    }
}
