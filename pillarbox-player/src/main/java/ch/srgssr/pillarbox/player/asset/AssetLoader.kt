/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * An abstract class responsible for loading an [Asset] from a given [MediaItem].
 *
 * @property mediaSourceFactory A factory for creating [MediaSource] instances.
 */
abstract class AssetLoader(val mediaSourceFactory: MediaSource.Factory) {
    /**
     * Determines if this [AssetLoader] is capable of loading an [Asset] from the provided [MediaItem].
     *
     * @param mediaItem The [MediaItem] representing the asset to potentially load.
     * @return Whether this [AssetLoader] can load an [Asset] from the provided [mediaItem].
     */
    abstract fun canLoadAsset(mediaItem: MediaItem): Boolean

    /**
     * Loads an asset based on the provided [MediaItem].
     *
     * @param mediaItem The [MediaItem] describing the media to load.
     * @return An [Asset] representing the loaded resource.
     */
    abstract suspend fun loadAsset(mediaItem: MediaItem): Asset

    /**
     * Encodes the [trackersData] of an [Asset] loaded by this [AssetLoader], so that it can be stored with a download, and the playback of
     * the downloaded media can be tracked offline. The data is decoded with [decodeOfflineTrackersData] when the downloaded media is played.
     *
     * The default implementation returns `null`: the playback of downloaded media is not tracked.
     *
     * @param trackersData The [MediaItemTrackerData] of the [Asset] loaded by [loadAsset].
     * @return The encoded data, or `null` if there is nothing to store.
     */
    open fun encodeOfflineTrackersData(trackersData: MediaItemTrackerData): ByteArray? = null

    /**
     * Decodes the data encoded with [encodeOfflineTrackersData], to track the playback of a downloaded media.
     *
     * The default implementation returns `null`: the playback of downloaded media is not tracked.
     *
     * @param data The data returned by [encodeOfflineTrackersData].
     * @return The [MediaItemTrackerData] to use while playing the downloaded media, or `null` if [data] can't be decoded.
     */
    open fun decodeOfflineTrackersData(data: ByteArray): MediaItemTrackerData? = null
}
