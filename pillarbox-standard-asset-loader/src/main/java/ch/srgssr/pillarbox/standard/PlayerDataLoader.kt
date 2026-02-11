/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import androidx.media3.common.MediaItem

interface PlayerDataLoader<CustomData> {
    fun canLoad(mediaItem: MediaItem): Boolean

    suspend fun load(mediaItem: MediaItem): Result<PlayerData<CustomData>>
}
