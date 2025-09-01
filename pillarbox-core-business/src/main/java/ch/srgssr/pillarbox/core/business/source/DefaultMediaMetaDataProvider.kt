/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * A [MediaMetadata.Builder] extension that populates its receiver with default values.
 *
 * @param metadata The underlying [MediaMetadata].
 * @param chapter The [Chapter] to extract data from.
 * @param mediaComposition The [MediaComposition] containing information about the media.
 *
 * @receiver A [MediaMetadata.Builder] created from the provided [MediaMetadata].
 */
val DefaultMediaMetaDataProvider: suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit =
    { metadata, chapter, mediaComposition ->
        metadata.title ?: setTitle(chapter.title)
        metadata.subtitle ?: setSubtitle(chapter.lead)
        metadata.description ?: setDescription(chapter.description)
        metadata.artworkUri ?: run {
            val artworkUri = imageScalingService.getScaledImageUrl(
                imageUrl = chapter.imageUrl
            ).toUri()
            setArtworkUri(artworkUri)
        }
    }

private val imageScalingService = ImageScalingService()
