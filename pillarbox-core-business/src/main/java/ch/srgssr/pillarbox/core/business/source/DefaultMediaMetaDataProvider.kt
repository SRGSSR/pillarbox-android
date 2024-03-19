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
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource

/**
 * A [SRGAssetLoader.MediaMetadataProvider] filling [MediaMetadata] from [Chapter].
 * Original MediaMetadata provided are not replaced.
 */
class DefaultMediaMetaDataProvider : SRGAssetLoader.MediaMetadataProvider {

    private val imageScalingService = ImageScalingService()

    override fun provide(mediaMetadataBuilder: MediaMetadata.Builder, resource: Resource, chapter: Chapter, mediaComposition: MediaComposition) {
        val metadata = mediaMetadataBuilder.build()
        metadata.title ?: mediaMetadataBuilder.setTitle(chapter.title)
        metadata.subtitle ?: mediaMetadataBuilder.setSubtitle(chapter.lead)
        metadata.description ?: mediaMetadataBuilder.setDescription(chapter.description)
        metadata.artworkUri ?: run {
            val artworkUri = imageScalingService.getScaledImageUrl(
                imageUrl = chapter.imageUrl
            ).toUri()
            mediaMetadataBuilder.setArtworkUri(artworkUri)
        }
    }
}
