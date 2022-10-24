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
     */
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
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
