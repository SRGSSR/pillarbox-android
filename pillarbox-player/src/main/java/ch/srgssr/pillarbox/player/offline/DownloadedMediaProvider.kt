/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import androidx.media3.common.MediaItem
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.DownloadRequest

/**
 * Provides the media that have been downloaded for offline playback.
 *
 * When set on the player with [PillarboxBuilder.downloadedMediaProvider][ch.srgssr.pillarbox.player.PillarboxBuilder.downloadedMediaProvider],
 * every [MediaItem] for which [getDownloadRequest] returns a value is played from [cache], instead of being loaded by the
 * [AssetLoader][ch.srgssr.pillarbox.player.asset.AssetLoader]s.
 */
interface DownloadedMediaProvider {
    /**
     * The [Cache] containing the downloaded media.
     */
    val cache: Cache

    /**
     * Returns the [DownloadRequest] used to download the given [MediaItem], or `null` if it has not been completely downloaded.
     *
     * @param mediaItem The [MediaItem] to look up.
     */
    fun getDownloadRequest(mediaItem: MediaItem): DownloadRequest?

    /**
     * Returns the trackers data stored with [downloadRequest], as encoded by
     * [AssetLoader.encodeOfflineTrackersData][ch.srgssr.pillarbox.player.asset.AssetLoader.encodeOfflineTrackersData], or `null` if none.
     *
     * The data is decoded by the [AssetLoader][ch.srgssr.pillarbox.player.asset.AssetLoader] able to load the played [MediaItem], so that
     * the playback of the downloaded media is tracked.
     *
     * The default implementation returns `null`: the playback of downloaded media is not tracked.
     *
     * @param downloadRequest The [DownloadRequest] returned by [getDownloadRequest].
     */
    fun getOfflineTrackersData(downloadRequest: DownloadRequest): ByteArray? = null
}
