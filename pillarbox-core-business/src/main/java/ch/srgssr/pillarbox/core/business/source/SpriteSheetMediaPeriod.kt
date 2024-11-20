/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.graphics.Bitmap
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
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [MediaPeriod] that loads a [Bitmap] and pass it to a [SampleStream].
 */
internal class SpriteSheetMediaPeriod(
    private val spriteSheet: SpriteSheet,
    private val spriteSheetLoader: SpriteSheetLoader = SpriteSheetLoader.Default()
) : MediaPeriod {
    private var bitmap: Bitmap? = null
    private val isLoading = AtomicBoolean(true)
    private val format = Format.Builder()
        .setId("SpriteSheet")
        .setFrameRate(1f / spriteSheet.interval.milliseconds.inWholeSeconds)
        .setCustomData(spriteSheet)
        .setRoleFlags(C.ROLE_FLAG_MAIN)
        .setContainerMimeType(MimeTypes.IMAGE_JPEG)
        .setSampleMimeType(MimeTypes.IMAGE_JPEG)
        .build()
    private val tracks = TrackGroupArray(TrackGroup("sprite-sheet-srg", format))
    private var positionUs = 0L

    override fun prepare(callback: MediaPeriod.Callback, positionUs: Long) {
        callback.onPrepared(this@SpriteSheetMediaPeriod)
        this.positionUs = positionUs
        isLoading.set(true)
        spriteSheetLoader.loadSpriteSheet(spriteSheet) { bitmap ->
            this.bitmap = bitmap
            isLoading.set(false)
        }
    }

    fun releasePeriod() {
        bitmap?.recycle()
        bitmap = null
    }

    override fun selectTracks(
        selections: Array<out ExoTrackSelection?>,
        mayRetainStreamFlags: BooleanArray,
        streams: Array<SampleStream?>,
        streamResetFlags: BooleanArray,
        positionUs: Long
    ): Long {
        this.positionUs = getAdjustedSeekPositionUs(positionUs, SeekParameters.DEFAULT)
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
        return C.TIME_UNSET
    }

    override fun getNextLoadPositionUs(): Long {
        return C.TIME_UNSET
    }

    override fun continueLoading(loadingInfo: LoadingInfo): Boolean {
        return isLoading()
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

        override fun isReady(): Boolean {
            return !isLoading()
        }

        override fun maybeThrowError() {
            if (bitmap == null && !isLoading.get()) {
                throw IOException("Can't decode ${spriteSheet.url}")
            }
        }

        @Suppress("ReturnCount")
        override fun readData(formatHolder: FormatHolder, buffer: DecoderInputBuffer, readFlags: Int): Int {
            if ((readFlags and SampleStream.FLAG_REQUIRE_FORMAT) != 0) {
                formatHolder.format = tracks[0].getFormat(0)
                return C.RESULT_FORMAT_READ
            }

            if (isLoading.get()) {
                return C.RESULT_NOTHING_READ
            }

            val intervalUs = spriteSheet.interval.milliseconds.inWholeMicroseconds
            val tileIndex = positionUs / intervalUs
            buffer.addFlag(C.BUFFER_FLAG_KEY_FRAME)
            buffer.timeUs = positionUs
            if (bitmap != null) {
                val data = cropTileFromImageGrid(max((tileIndex.toInt() - 1), 0))
                buffer.ensureSpaceForWrite(data.size)
                buffer.data?.put(data, /* offset= */0, data.size)
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
            val tile = Bitmap.createBitmap(bitmap!!, tileStartXCoordinate, tileStartYCoordinate, tileWidth, tileHeight)
            return bitmapToByteArray(tile)
        }
    }

    private companion object {
        private const val MAX_QUALITY = 100

        private fun bitmapToByteArray(
            bitmap: Bitmap,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
            quality: Int = MAX_QUALITY
        ): ByteArray {
            return ByteArrayOutputStream().use {
                bitmap.compress(format, quality, it)
                it.toByteArray()
            }
        }
    }
}
