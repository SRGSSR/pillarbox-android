/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import androidx.media3.common.MediaItem

/**
 * Interface for loading [PlayerData] with a given [MediaItem].
 */
interface PlayerDataLoader<CustomData> {
    /**
     * Checks if the given [MediaItem] can be loaded by this loader.
     */
    fun canLoad(mediaItem: MediaItem): Boolean

    /**
     * Loads the [PlayerData] for the given [MediaItem].
     */
    suspend fun load(mediaItem: MediaItem): Result<PlayerData<CustomData>>
}
