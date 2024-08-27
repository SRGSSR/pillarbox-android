/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * Assets
 *
 * @property mediaSource The [MediaSource] used by the player to play something.
 * @property trackersData The [MediaItemTrackerData] to set to the [PillarboxData].
 * @property mediaMetadata The [MediaMetadata] to set to the player media item.
 * @property blockedTimeRanges The [BlockedTimeRange] list.
 */
data class Asset(
    val mediaSource: MediaSource,
    val trackersData: MediaItemTrackerData = MediaItemTrackerData.EMPTY,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val blockedTimeRanges: List<BlockedTimeRange> = emptyList(),
)
