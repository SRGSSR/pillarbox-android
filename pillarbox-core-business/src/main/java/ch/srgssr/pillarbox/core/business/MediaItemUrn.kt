/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * Create [MediaItem] for Pillarbox from a urn.
 */
object MediaItemUrn {
    /**
     * Invoke
     *
     * @param urn The media urn to play.
     * @param title The optional title to display..
     * @param subtitle The optional subtitle to display.
     * @param artworkUri The artworkUri image uri.
     * @return MediaItem.
     */
    operator fun invoke(urn: String, title: String? = null, subtitle: String? = null, artworkUri: Uri? = null): MediaItem {
        return MediaItem.Builder()
            .setMediaId(urn)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    // TODO Integrate `ImageScaleService` once we know if this class will be used or not
                    .setArtworkUri(artworkUri)
                    .build()
            )
            .build()
    }
}
