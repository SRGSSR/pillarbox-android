/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource

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
}
