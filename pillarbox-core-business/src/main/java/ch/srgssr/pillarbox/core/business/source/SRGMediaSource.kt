/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.exoplayer.FormatHolder
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SampleStream
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.source.WrappingMediaSource
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import java.net.URL
import java.nio.ByteBuffer

class SRGMediaSource(mediaSource: MediaSource, private val spriteSheet: SpriteSheet? = null) : WrappingMediaSource(mediaSource) {

    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        return SRGMediaPeriod(super.createPeriod(id, allocator, startPositionUs), spriteSheet)
    }

    private class SRGMediaPeriod(private val mediaPeriod: MediaPeriod, private val spriteSheet: SpriteSheet? = null) :
        MediaPeriod by
        mediaPeriod {
        private val spriteSheetFormat: Format? = spriteSheet?.let {
            // FIXME we make the hypotes it is png
            val mimeType = MimeTypes.IMAGE_JPEG
            Format.Builder()
                .setId(it.urn)
                .setTileCountVertical(it.rows)
                .setTileCountHorizontal(it.columns)
                .setWidth(it.thumbnailWidth * it.columns)
                .setHeight(it.thumbnailHeight * it.rows)
                .setFrameRate(1f / (it.interval / 1000f))
                .setPeakBitrate(1)
                .setCustomData(it)
                .setRoleFlags(C.ROLE_FLAG_MAIN)
                .setContainerMimeType(mimeType)
                .setSampleMimeType(mimeType)
                .build()
        }
        private val customTrackGroup = buildList<TrackGroup> {
            spriteSheetFormat?.let {
                add(
                    TrackGroup(
                        "IL-Sprite-sheet:",
                        it
                    )
                )
            }
        }.toTypedArray()
        private var spriteSheetSampleStream: SpriteSheetSampleStream? = null

        override fun selectTracks(
            selections: Array<out ExoTrackSelection?>,
            mayRetainStreamFlags: BooleanArray,
            streams: Array<out SampleStream?>,
            streamResetFlags: BooleanArray,
            positionUs: Long
        ): Long {
            if (spriteSheetFormat == null) return mediaPeriod.selectTracks(selections, mayRetainStreamFlags, streams, streamResetFlags, positionUs)
            // Recreate selection and streams for underlying mediaPeriod
            val sourceSelections = Array(selections.size - 1) { index ->
                selections[index]
            }
            val sourceSampleStream = Array(streams.size - 1) { sampleIndex ->
                streams[sampleIndex]
            }

            val p = mediaPeriod.selectTracks(sourceSelections, mayRetainStreamFlags, sourceSampleStream, streamResetFlags, positionUs)

            // Create sample stream for custom tracks, currently EmptySampleStream but could be more complicated, by streaming chapters.
            val sampleStream = Array(streams.size) { sampleIndex ->
                // No SampleStream for disabled tracks, ie selection is null.
                val format = selections[sampleIndex]?.selectedFormat
                if (sampleIndex == streams.size - 1) {
                    spriteSheetSampleStream = if (selections[sampleIndex] != null && format != null) {
                        SpriteSheetSampleStream(
                            format
                        )
                    } else {
                        null
                    }
                    spriteSheetSampleStream
                } else {
                    sourceSampleStream[sampleIndex]
                }
            }
            System.arraycopy(sampleStream, 0, streams, 0, streams.size)
            return p
        }

        @Suppress("SpreadOperator")
        override fun getTrackGroups(): TrackGroupArray {
            val trackGroups = mediaPeriod.trackGroups
            val trackGroupArray = Array(trackGroups.length) {
                trackGroups.get(it)
            }
            // Don't know how to do it, without SpreadOperator!
            return TrackGroupArray(*trackGroupArray, *customTrackGroup)
        }

        override fun seekToUs(positionUs: Long): Long {
            val seekTo = mediaPeriod.seekToUs(positionUs)
            spriteSheetSampleStream?.seek(seekTo)
            return seekTo
        }
    }

    class SpriteSheetSampleStream(val format: Format) : SampleStream {
        private var positionUs: Long = 0
        private var isFormatSend = false
        private var dataRead: Boolean = false

        override fun isReady(): Boolean {
            return true
        }

        override fun maybeThrowError() {
        }

        fun seek(positionUs: Long) {
            this.positionUs = positionUs
        }

        override fun readData(formatHolder: FormatHolder, buffer: DecoderInputBuffer, readFlags: Int): Int {
            val spriteSheet = format.customData as SpriteSheet?
            if (spriteSheet == null || dataRead) {
                buffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM)
                return C.RESULT_BUFFER_READ
            }
            if (readFlags.and(SampleStream.FLAG_REQUIRE_FORMAT) == SampleStream.FLAG_REQUIRE_FORMAT || !isFormatSend) {
                formatHolder.format = format
                isFormatSend = true
                return C.RESULT_FORMAT_READ
            }
            // if flag not flag omit sample data
            if (readFlags.and(SampleStream.FLAG_OMIT_SAMPLE_DATA) == 0) {
                URL(spriteSheet.url).openStream().use {
                    val bytes: ByteArray = it.readAllBytes()
                    buffer.ensureSpaceForWrite(bytes.size)
                    buffer.data = ByteBuffer.wrap(bytes)
                    dataRead = true
                    Log.d("Coucou", "Image loaded")
                }
            }
            buffer.timeUs = 0L
            buffer.setFlags(C.BUFFER_FLAG_KEY_FRAME)
            return C.RESULT_BUFFER_READ
        }

        override fun skipData(positionUs: Long): Int {
            return 0
        }
    }
}
