/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import kotlinx.coroutines.flow.Flow

/**
 * Represents an asset, which contains the necessary components for playback.
 *
 * @property mediaSource The [MediaSource] used by the player to play the content.
 * @property trackersData The [MediaItemTrackerData] containing information for tracking playback events and metrics.
 * @property mediaMetadata The [MediaMetadata] providing descriptive information about the media item, such as title, artist, etc.
 * @property pillarboxMetadata A [PillarboxMetadata] instance containing additional information about the asset.
 * @property responseHeaders A [Map<String, List<String>>] The response headers associated with the asset load, or an empty map if unavailable.
 * @property updateFlow An optional [Flow] of [UpdatableMediaItem] emitting updates of the asset while it is playing,
 * or `null` if the asset never changes.
 */
data class Asset(
    val mediaSource: MediaSource,
    val trackersData: MediaItemTrackerData = MutableMediaItemTrackerData.EMPTY.toMediaItemTrackerData(),
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val pillarboxMetadata: PillarboxMetadata = PillarboxMetadata.EMPTY,
    val responseHeaders: Map<String, List<String>> = emptyMap(),
    val updateFlow: Flow<UpdatableMediaItem>? = null,
)

/**
 * Represents an update of an [Asset] while it is playing.
 *
 * Any change is applied to the item being played: a new [MediaMetadata] or [PillarboxMetadata] is applied immediately, and a new stream URL
 * reloads the item.
 *
 * @property pillarboxMetadata The updated [PillarboxMetadata].
 * @property mediaItem The [MediaItem] of the updated stream. Its URI is compared with the URI of the stream being played.
 * @property mediaMetadata The updated [MediaMetadata].
 */
data class UpdatableMediaItem(
    val pillarboxMetadata: PillarboxMetadata,
    val mediaItem: MediaItem,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
)
