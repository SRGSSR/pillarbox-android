/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srg.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srg.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srg.pillarbox.core.business.integrationlayer.data.Drm
import ch.srg.pillarbox.core.business.integrationlayer.data.MediaUrn
import ch.srg.pillarbox.core.business.integrationlayer.data.Resource
import ch.srg.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.RemoteResult
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Load [MediaItem] playable from a [ch.srg.pillarbox.core.business.integrationlayer.data.MediaComposition]
 *
 * Load a [MediaItem] from it's urn set in [MediaItem.mediaId] property.
 * Fill [MediaItem.mediaMetadata] from [MediaItem] if the field is not already set :
 * - [MediaMetadata.title] with [Chapter.title]
 * - [MediaMetadata.subtitle] with [Chapter.lead]
 * - [MediaMetadata.description] with [Chapter.description]
 *
 * @property mediaCompositionDataSource
 */
class MediaCompositionMediaItemSource(private val mediaCompositionDataSource: MediaCompositionDataSource) : MediaItemSource {
    private val resourceSelector = ResourceSelector()

    private fun fillMetaData(metadata: MediaMetadata, chapter: Chapter): MediaMetadata {
        val builder = metadata.buildUpon()
        metadata.title ?: builder.setTitle(chapter.title)
        metadata.subtitle ?: builder.setSubtitle(chapter.lead)
        metadata.description ?: builder.setDescription(chapter.description)
        metadata.artworkUri ?: builder.setArtworkUri(Uri.parse(chapter.imageUrl))
        // Extras are forwarded to MediaController, but not involve in the equality checks
        // builder.setExtras(extras)
        return builder.build()
    }

    private fun fillDrmConfiguration(resource: Resource): MediaItem.DrmConfiguration? {
        val drm = resource.drmList.orEmpty().find { it.type == Drm.Type.WIDEVINE }
        return drm?.let {
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(it.licenseUrl)
                .build()
        }
    }

    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        if (!MediaUrn.isValid(mediaItem.mediaId)) {
            throw IllegalArgumentException("Invalid urn=${mediaItem.mediaId}")
        }
        val mediaUri = mediaItem.localConfiguration?.uri
        if (mediaUri != null && MediaUrn.isValid(mediaUri.toString())) {
            throw IllegalArgumentException("Uri can't be a urn")
        }
        when (val result = mediaCompositionDataSource.getMediaCompositionByUrn(mediaItem.mediaId)) {
            is RemoteResult.Success -> {
                val chapter = result.data.mainChapter
                if (!chapter.blockReason.isNullOrEmpty()) {
                    throw BlockReasonException(chapter.blockReason)
                }
                val resource = resourceSelector.selectResourceFromChapter(chapter) ?: throw ResourceNotFoundException
                var uri = Uri.parse(resource.url)
                if (resource.tokenType == Resource.TokenType.AKAMAI) {
                    uri = appendTokenQueryToUri(uri)
                }
                return mediaItem.buildUpon()
                    .setMediaMetadata(fillMetaData(mediaItem.mediaMetadata, chapter))
                    .setDrmConfiguration(fillDrmConfiguration(resource))
                    .setTag(result)
                    .setUri(uri)
                    .build()
            }
            is RemoteResult.Error -> {
                throw result.throwable
            }
        }
    }

    /**
     * Select a [Resource] from [Chapter.listResource]
     */
    class ResourceSelector {
        /**
         * Select the first resource from chapter that is playable by the Player.
         *
         * @param chapter
         * @return null if no compatible resource is found.
         */
        @Suppress("SwallowedException")
        fun selectResourceFromChapter(chapter: Chapter): Resource? {
            return try {
                chapter.listResource?.first {
                    (it.type == Resource.Type.DASH || it.type == Resource.Type.HLS || it.type == Resource.Type.PROGRESSIVE) &&
                        (it.drmList == null || it.drmList.find { drm -> drm.type == Drm.Type.WIDEVINE } != null)
                }
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }

    companion object {
        /**
         * Token Query Param to add to trigger token request
         */
        const val TOKEN_QUERY_PARAM = "withToken"

        private fun appendTokenQueryToUri(uri: Uri): Uri {
            return uri.buildUpon().appendQueryParameter(TOKEN_QUERY_PARAM, "true").build()
        }
    }
}
