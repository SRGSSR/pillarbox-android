/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast.player

import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import com.google.android.gms.cast.MediaQueueItem

/**
 * Pillarbox media item converter from Exoplayer that allow MediaItem without mime type.
 */
class DefaultItemConverterWithoutMimeType(
    private val defaultConverter: DefaultMediaItemConverter = DefaultMediaItemConverter()
) :
    MediaItemConverter by defaultConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val item = if (mediaItem.localConfiguration?.mimeType == null) {
            mediaItem.buildUpon().setMimeType(MimeTypes.BASE_TYPE_APPLICATION).build()
        } else {
            mediaItem
        }
        return defaultConverter.toMediaQueueItem(item)
    }
}
