/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader

/**
 * [AssetLoader] that plays URLs from the [DemoDownloadService] cache, falling back to the network for anything not downloaded.
 *
 * @param context The context.
 */
class DownloadAssetLoader(context: Context) : AssetLoader(
    DefaultMediaSourceFactory(context)
        .setDataSourceFactory(DemoDownloadService.getCacheDataSourceFactory(context))
) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        // Only URLs can be downloaded, URNs are left to the SRG asset loader.
        val scheme = mediaItem.localConfiguration?.uri?.scheme
        return scheme == "http" || scheme == "https"
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(mediaItem),
            mediaMetadata = mediaItem.mediaMetadata,
        )
    }
}
