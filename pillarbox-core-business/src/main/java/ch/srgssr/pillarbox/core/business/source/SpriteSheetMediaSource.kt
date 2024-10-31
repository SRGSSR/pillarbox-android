/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.datasource.TransferListener
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.exoplayer.FormatHolder
import androidx.media3.exoplayer.LoadingInfo
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SampleStream
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import io.ktor.utils.io.charsets.Charsets
import kotlin.time.Duration.Companion.milliseconds

class SpriteSheetMediaSource(private val mediaItem: MediaItem, private val spriteSheet: SpriteSheet) : BaseMediaSource() {
    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun maybeThrowSourceInfoRefreshError() {
    }

    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        return ExternalTiledImageMediaPeriod(spriteSheet)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        // Nothing
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        val d = spriteSheet.rows * spriteSheet.columns * spriteSheet.interval
        val timeline = SinglePeriodTimeline(d.milliseconds.inWholeMicroseconds, true, false, false, null, getMediaItem())
        refreshSourceInfo(timeline)
    }

    override fun releaseSourceInternal() {
    }

    override fun canUpdateMediaItem(mediaItem: MediaItem): Boolean {
        return true
    }
}

class ExternalTiledImageMediaPeriod(private val spriteSheet: SpriteSheet) : MediaPeriod {
    private var sampleData: ByteArray = spriteSheet.url.toByteArray(Charsets.UTF_8)
    private val format = spriteSheet.let {
        Format.Builder()
            .setId(it.urn)
            .setTileCountVertical(it.rows)
            .setTileCountHorizontal(it.columns)
            .setWidth(it.thumbnailWidth * it.columns)
            .setHeight(it.thumbnailHeight * it.rows)
            .setCustomData(it)
            .setRoleFlags(C.ROLE_FLAG_MAIN)
            .setContainerMimeType(MimeTypes.IMAGE_JPEG)
            .setSampleMimeType(MimeTypes.APPLICATION_EXTERNALLY_LOADED_IMAGE)
            .build()
    }
    private val tracks = TrackGroupArray((TrackGroup("sprite-sheet-srg", format)))

    override fun prepare(callback: MediaPeriod.Callback, positionUs: Long) {
        callback.onPrepared(this)
    }

    override fun getTrackGroups(): TrackGroupArray {
        return tracks
    }

    override fun selectTracks(
        selections: Array<out ExoTrackSelection?>,
        mayRetainStreamFlags: BooleanArray,
        streams: Array<SampleStream?>,
        streamResetFlags: BooleanArray,
        positionUs: Long
    ): Long {
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

    override fun getBufferedPositionUs(): Long {
        return C.TIME_END_OF_SOURCE
    }

    override fun getNextLoadPositionUs(): Long {
        return C.TIME_END_OF_SOURCE
    }

    override fun continueLoading(loadingInfo: LoadingInfo): Boolean {
        return false
    }

    override fun isLoading(): Boolean {
        return false
    }

    override fun reevaluateBuffer(positionUs: Long) {
        // Nothing
    }

    override fun maybeThrowPrepareError() {
    }

    override fun discardBuffer(positionUs: Long, toKeyframe: Boolean) {
    }

    override fun readDiscontinuity(): Long {
        return C.TIME_UNSET
    }

    override fun seekToUs(positionUs: Long): Long {
        return positionUs
    }

    override fun getAdjustedSeekPositionUs(positionUs: Long, seekParameters: SeekParameters): Long {
        return positionUs
    }

    private inner class SpriteSheetSampleStream : SampleStream {
        private var streamState = STREAM_STATE_SEND_FORMAT

        override fun isReady(): Boolean {
            return true
        }

        override fun maybeThrowError() {
        }

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

            val sampleSize = sampleData.size
            buffer.addFlag(C.BUFFER_FLAG_KEY_FRAME)
            buffer.timeUs = 0
            if ((readFlags and SampleStream.FLAG_OMIT_SAMPLE_DATA) == 0) {
                buffer.ensureSpaceForWrite(sampleSize)
                buffer.data!!.put(sampleData, /* offset= */0, sampleSize)
            }
            if ((readFlags and SampleStream.FLAG_PEEK) == 0) {
                streamState = STREAM_STATE_END_OF_STREAM
            }
            return C.RESULT_BUFFER_READ
        }

        override fun skipData(positionUs: Long): Int {
            return 0
        }
    }
}

private const val STREAM_STATE_SEND_FORMAT: Int = 0
private const val STREAM_STATE_SEND_SAMPLE: Int = 1
private const val STREAM_STATE_END_OF_STREAM: Int = 2
