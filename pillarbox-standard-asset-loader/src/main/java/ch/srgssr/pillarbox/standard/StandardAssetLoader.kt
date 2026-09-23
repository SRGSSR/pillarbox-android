/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

/**
 * Standard [AssetLoader] implementation.

 * @param CustomData The type of custom data associated with the [PlayerData].
 * @param playerDataLoader The [PlayerDataLoader] to use for loading [PlayerData].
 * @param playerDataMapper The [PlayerDataMapper] to use for mapping [PlayerData] to [Asset].
 * @param mediaSourceFactory The [MediaSource.Factory] to use for creating [MediaSource].
 */
class StandardAssetLoader<CustomData> constructor(
    private val playerDataLoader: PlayerDataLoader<CustomData>,
    private val playerDataMapper: PlayerDataMapper<CustomData>,
    mediaSourceFactory: MediaSource.Factory,
) : AssetLoader(mediaSourceFactory) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return playerDataLoader.canLoad(mediaItem)
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaInfo = playerDataLoader.load(mediaItem).getOrThrow()
        if (mediaInfo.source == null) throw NoSourceException()
        val playableMediaItem = MediaItem.Builder().apply {
            setUri(mediaInfo.source.url)
            setMimeType(mediaInfo.source.mimeType)
            setDrmConfiguration(mediaInfo.drm?.toDrmConfig())
        }.build()
        val mutableMediaItemTrackerData = MutableMediaItemTrackerData.EMPTY
        with(playerDataMapper) {
            mediaInfo.mediaItemTrackerData(mutableMediaItemTrackerData)
        }
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(playableMediaItem),
            trackersData = mutableMediaItemTrackerData.toMediaItemTrackerData(),
            pillarboxMetadata = with(playerDataMapper) { mediaInfo.pillarboxMetadata() },
            mediaMetadata = with(playerDataMapper) { mediaInfo.mediaMetadata() },
        )
    }

    companion object {
        private fun PlayerData.Drm.toDrmConfig(): MediaItem.DrmConfiguration {
            return MediaItem.DrmConfiguration.Builder(keySystem.toUUID())
                .setLicenseUri(licenseUrl)
                .setMultiSession(multisession)
                .build()
        }
    }
}
