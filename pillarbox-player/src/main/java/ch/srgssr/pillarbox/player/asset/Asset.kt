/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

/**
 * Represents an asset, which contains the necessary components for playback.
 *
 * @property mediaSource The [MediaSource] used by the player to play the content.
 * @property trackersData The [MediaItemTrackerData] containing information for tracking playback events and metrics.
 * @property mediaMetadata The [MediaMetadata] providing descriptive information about the media item, such as title, artist, etc.
 * @property pillarboxMetadata A [Metadata] instance containing additional information about the asset.
 */
data class Asset(
    val mediaSource: MediaSource,
    val trackersData: MediaItemTrackerData = MutableMediaItemTrackerData.EMPTY.toMediaItemTrackerData(),
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val pillarboxMetadata: PillarboxMetadata = PillarboxMetadata.EMPTY,
)
