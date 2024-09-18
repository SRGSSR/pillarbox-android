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
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.PILLARBOX_BLOCKED_MIME_TYPE
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.PILLARBOX_TRACKERS_MIME_TYPE
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

internal class PillarboxMediaPeriod(
    private val mediaPeriod: MediaPeriod,
    mediaItemTrackerData: MediaItemTrackerData,
    blockedTimeRanges: List<BlockedTimeRange>,
) : MediaPeriod by mediaPeriod {
    private val pillarboxTracks = mutableListOf<TrackGroup>().apply {
        if (mediaItemTrackerData.isNotEmpty) {
            add(
                TrackGroup(
                    "Pillarbox-Trackers",
                    Format.Builder()
                        .setId("TrackerData:0")
                        .setSampleMimeType(PILLARBOX_TRACKERS_MIME_TYPE)
                        .setCustomData(mediaItemTrackerData)
                        .build(),
                )
            )
        }
        if (blockedTimeRanges.isNotEmpty()) {
            TrackGroup(
                "Pillarbox-BlockedTimeRanges",
                Format.Builder()
                    .setSampleMimeType(PILLARBOX_BLOCKED_MIME_TYPE)
                    .setId("BlockedTimeRanges")
                    .setCustomData(blockedTimeRanges)
                    .build(),
            )
        }
    }.toTypedArray()

    @Suppress("SpreadOperator")
    override fun getTrackGroups(): TrackGroupArray {
        val trackGroups = mediaPeriod.trackGroups
        val trackGroupArray = Array(trackGroups.length) {
            trackGroups.get(it)
        }
        // Don't know how to do it, without SpreadOperator!
        return TrackGroupArray(*trackGroupArray, *pillarboxTracks)
    }

    fun release(mediaSource: MediaSource) {
        mediaSource.releasePeriod(mediaPeriod)
    }
}
