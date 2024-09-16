/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.EmptySampleStream
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SampleStream
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.PILLARBOX_TRACK_MIME_TYPE

internal class PillarboxMediaPeriod(
    private val mediaPeriod: MediaPeriod,
    pillarboxData: PillarboxData,
) : MediaPeriod by mediaPeriod {
    private val pillarboxGroup: TrackGroup = TrackGroup(
        "Pillarbox",
        Format.Builder()
            .setId("PillarboxData")
            .setSampleMimeType(PILLARBOX_TRACK_MIME_TYPE)
            .setCustomData(pillarboxData)
            .build(),
    )

    override fun selectTracks(
        selections: Array<out ExoTrackSelection?>,
        mayRetainStreamFlags: BooleanArray,
        streams: Array<out SampleStream?>,
        streamResetFlags: BooleanArray,
        positionUs: Long
    ): Long {
        // Recreate selection and streams for underlying mediaPeriod
        val sourceSelections = Array(selections.size - 1) { index ->
            selections[index]?.let {
                if (it.trackGroup.type > C.TRACK_TYPE_CUSTOM_BASE) {
                    null
                } else {
                    it
                }
            }
        }
        val sourceSampleStream = Array(streams.size - 1) { sampleIndex ->
            streams[sampleIndex]
        }

        val p = mediaPeriod.selectTracks(sourceSelections, mayRetainStreamFlags, sourceSampleStream, streamResetFlags, positionUs)

        // Create sample stream for custom tracks, currently EmptySampleStream but could be more complicated.
        val sampleStream = Array(streams.size) { sampleIndex ->
            // No SampleStream for disabled tracks, i.e., selection is null.
            if (sampleIndex == streams.size - 1) if (selections[sampleIndex] != null) EmptySampleStream() else null
            else {
                sourceSampleStream[sampleIndex]
            }
        }
        System.arraycopy(sampleStream, 0, streams, 0, streams.size)
        return p
    }

    @Suppress("SpreadOperator")
    override fun getTrackGroups(): TrackGroupArray {
        val trackGroup = mediaPeriod.trackGroups
        val trackGroups = Array(trackGroup.length + 1) {
            if (it < trackGroup.length) {
                trackGroup.get(it)
            } else {
                pillarboxGroup
            }
        }
        // Don't know how to do it, without SpreadOperator!
        return TrackGroupArray(*trackGroups)
    }

    fun release(mediaSource: MediaSource) {
        mediaSource.releasePeriod(mediaPeriod)
    }
}
