/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Load MediaItem from [urnMediaItemSource] if the [MediaItem.mediaId] is an urn.
 *
 * In the demo application we are mixing url and urn. To simplify the data, we choose to store
 * urn and url in the [DemoItem.uri] which provide a why to convert it to [MediaItem].
 *
 * @property urnMediaItemSource item source to use with urn
 */
class MixedMediaItemSource(
    private val urnMediaItemSource: MediaCompositionMediaItemSource
) : MediaItemSource {

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        return if (mediaItem.mediaId.isValidMediaUrn()) {
            urnMediaItemSource.loadMediaItem(mediaItem)
        } else {
            mediaItem
        }
    }
}
