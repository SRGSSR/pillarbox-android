/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
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
