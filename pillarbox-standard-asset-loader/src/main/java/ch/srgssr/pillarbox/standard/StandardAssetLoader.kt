/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

class StandardAssetLoader<CustomData>(
    private val playerDataLoader: PlayerDataLoader<CustomData>,
    private val playerDataMapper: PlayerDataMapper<CustomData>,
    mediaSourceFactory: MediaSource.Factory,
) : AssetLoader(mediaSourceFactory) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return playerDataLoader.canLoad(mediaItem)
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaInfo = playerDataLoader.load(mediaItem)
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
            val drmUUID = when (keySystem) {
                PlayerData.Drm.KeySystem.WIDEVINE -> C.WIDEVINE_UUID
                PlayerData.Drm.KeySystem.PLAYREADY -> C.PLAYREADY_UUID
                PlayerData.Drm.KeySystem.CLEAR_KEY -> C.CLEARKEY_UUID
            }
            return MediaItem.DrmConfiguration.Builder(drmUUID)
                .setLicenseUri(licenseUrl)
                .setMultiSession(multisession)
                .build()
        }
    }
}
