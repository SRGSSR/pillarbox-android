/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource

/**
 * Asset loader
 *
 * @property mediaSourceFactory
 * @constructor Create empty Asset loader
 */
abstract class AssetLoader(val mediaSourceFactory: MediaSource.Factory) {
    /**
     * Can load asset
     *
     * @param mediaItem The input [MediaItem].
     * @return true if this AssetLoader can load an Asset from the mediaItem.
     */
    abstract fun canLoadAsset(mediaItem: MediaItem): Boolean

    /**
     * Load asset
     *
     * @param mediaItem The input [MediaItem]
     * @return a [Asset].
     */
    abstract suspend fun loadAsset(mediaItem: MediaItem, asset: Asset.Builder)

    // OnLoadComplete => tracker.start(data) / OnLoadError => tracker.onError(mediaItem(id:1234),error,periodUid)
    // List trackers to use with this MediaItem. 1 list per mediaItem
    // abstract fun getTrackers(mediaItem: MediaItem): List<MediaItemTracker> = emptyList()
}
