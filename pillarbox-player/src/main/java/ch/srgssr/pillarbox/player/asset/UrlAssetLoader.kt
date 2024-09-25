/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * [AssetLoader] to load an [Asset] from a stream url.
 *
 * @param defaultMediaSourceFactory The [DefaultMediaSourceFactory] to create a MediaSource for the player.
 */
class UrlAssetLoader(
    defaultMediaSourceFactory: DefaultMediaSourceFactory,
) : AssetLoader(defaultMediaSourceFactory) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
        return Asset(
            mediaSource = mediaSource,
            mediaMetadata = mediaItem.mediaMetadata,
        )
    }
}
