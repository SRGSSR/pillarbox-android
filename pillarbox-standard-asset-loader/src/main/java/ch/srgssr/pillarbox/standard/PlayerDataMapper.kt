/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import java.util.UUID

/**
 * Interface for mapping [PlayerData] to Pillarbox data structure.
 */
interface PlayerDataMapper<CustomData> {

    /**
     * Maps [PlayerData] to [PillarboxMetadata].
     */
    fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata

    /**
     * Maps [PlayerData] to [MediaMetadata].
     */
    fun PlayerData<CustomData>.mediaMetadata(): MediaMetadata

    /**
     * Maps [PlayerData] to [MutableMediaItemTrackerData].
     */
    fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData)

    /**
     * Default implementation of [PlayerDataMapper].
     */
    class Default<CustomData> : PlayerDataMapper<CustomData> {
        override fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata {
            val chapters = chapters?.let { listChapters ->
                listChapters.mapIndexed { index, chapter ->
                    Chapter(
                        chapter.identifier ?: "$index",
                        chapter.startTime,
                        chapter.endTime,
                        MediaMetadata.Builder().apply {
                            setTitle(chapter.title)
                            setArtworkUri(chapter.title?.toUri())
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
                posterUrl?.let { setArtworkUri(it.toUri()) }
            }.build()
        }

        override fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData) = Unit
    }
}

/**
 * @return the [UUID] to use when creating [androidx.media3.common.MediaItem.DrmConfiguration.Builder].
 */
fun PlayerData.Drm.KeySystem.toUUID(): UUID {
    return when (this) {
        PlayerData.Drm.KeySystem.WIDEVINE -> C.WIDEVINE_UUID
        PlayerData.Drm.KeySystem.PLAYREADY -> C.PLAYREADY_UUID
        PlayerData.Drm.KeySystem.CLEAR_KEY -> C.CLEARKEY_UUID
    }
}
