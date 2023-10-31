/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.data

import androidx.media3.common.MediaItem

/**
 * Media item source load MediaItem with a suspend function.
 */
interface MediaItemSource {
    /**
     * Load media item from [mediaItem] in a suspend function
     *
     * @param mediaItem
     * @return MediaItem buildUpon [mediaItem]
     */
    suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem

    fun handle(mediaItem: MediaItem): Boolean = true
}
