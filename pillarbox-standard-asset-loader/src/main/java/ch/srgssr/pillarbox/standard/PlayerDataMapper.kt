/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

interface PlayerDataMapper<CustomData> {

    fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata

    fun PlayerData<CustomData>.mediaMetadata(): MediaMetadata

    fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData)

    class Default<CustomData> : PlayerDataMapper<CustomData> {
        override fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata {
            val chapters = chapters?.let { listChapters ->
                listChapters.map {
                    Chapter(
                        it.identifier ?: "",
                        it.startTime,
                        it.endTime,
                        MediaMetadata.Builder().apply {
                            setTitle(it.title)
                            setArtworkUri(it.title?.toUri())
                        }.build()
                    )
                }
            }
            val blocked = timeRanges?.filter { it.isBlocked() }?.map {
                BlockedTimeRange(it.startTime, it.endTime, reason = it.type)
            }
            val credits = timeRanges?.filter { it.isOpeningCredits() || it.isClosingCredits() }?.map {
                if (it.isOpeningCredits()) {
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

        override fun PlayerData<CustomData>.mediaMetadata(): MediaMetadata {
            return MediaMetadata.Builder().apply {
                title?.let {
                    setTitle(it)
                    setDisplayTitle(it)
                }
                subtitle?.let { setSubtitle(it) }
                posterUrl?.let { setArtworkUri(Uri.parse(it)) }
            }.build()
        }

        override fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData) = Unit
    }
}
