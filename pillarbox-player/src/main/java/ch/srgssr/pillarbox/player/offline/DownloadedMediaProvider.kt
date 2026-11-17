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
}
