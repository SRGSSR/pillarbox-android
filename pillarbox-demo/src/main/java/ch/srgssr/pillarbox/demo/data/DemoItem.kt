/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.Serializable

/**
 * Demo item
 *
 * @property title
 * @property uri
 * @property description
 */
data class DemoItem(val title: String, val uri: String, val description: String? = null) : Serializable {
    /**
     * Convert to a [MediaItem]
     * When [uri] is a Urn, set [MediaItem.Builder.setUri] to null,
     * Urn ItemSource need to have a urn defined in [MediaItem.mediaId] not its uri.
     */
    fun toMediaItem(): MediaItem {
        val uri: String? = if (this.uri.startsWith("urn:")) null else this.uri
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(this.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    .build()
            )
            .build()
    }

    companion object {
        private const val serialVersionUID: Long = 1
    }
}
