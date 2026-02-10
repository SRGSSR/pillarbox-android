/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.source

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import ch.srgssr.pillarbox.standard.PlayerData
import ch.srgssr.pillarbox.standard.PlayerDataMapper

class PillarboxDemoMapper : PlayerDataMapper<CustomData>() {

    override fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData) {
        customData?.let {
            // Configure your damn trackers here!
            // mutableMediaItemTrackerData[ComScoreTracker::class.java] = FactoryData(factory = ComScoreTracker.Factory(),data = ComScoreTracker.Data(emptyMap()))
        }
    }

    override fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata {
        val chapters = chapters?.let { listChapters ->
            listChapters.map {
                Chapter(
                    it.identifier ?: "",
                    it.startTime,
                    it.endTime,
                    MediaMetadata.Builder().apply {
                        setTitle(it.title)
                        setArtworkUri(it.title?.toUri()) // Maybe using a image service here!
                    }.build()
                )
            }
        }
        val blocked = timeRanges?.filter { it.type == "blocked" }?.map {
            BlockedTimeRange(it.startTime, it.endTime, reason = it.type)
        }
        // TODO remove credits with a more generic event time ranges.
        val credits = timeRanges?.filter { it.type == "start_credit" || it.type == "end_credit" }?.map {
            if (it.type == "start_credit") {
                Credit.Opening(it.startTime, it.endTime)
            } else {
                Credit.Closing(it.startTime, it.endTime)
            }
        }
        return PillarboxMetadata(
            chapters = chapters ?: emptyList(),
            blockedTimeRanges = blocked ?: emptyList(),
            credits = credits ?: emptyList()
        )
    }
}
