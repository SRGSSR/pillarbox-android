/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.data

import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * Media item source load MediaItem as a Flow
 */
interface MediaItemSource {
    /**
     * Emit media item from [mediaItem] in a Flow
     *
     * @param mediaItem
     * @return MediaItem buildUpon [mediaItem]
     */
    fun loadMediaItem(mediaItem: MediaItem): Flow<MediaItem>
}
