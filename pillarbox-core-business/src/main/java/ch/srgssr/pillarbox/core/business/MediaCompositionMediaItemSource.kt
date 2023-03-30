/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.RemoteResult
import ch.srgssr.pillarbox.core.business.tracker.ComScoreTracker
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.setTrackerData

/**
 * Load [MediaItem] playable from a [ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition]
 *
 * Load a [MediaItem] from it's urn set in [MediaItem.mediaId] property.
 * Fill [MediaItem.mediaMetadata] from [MediaItem] if the field is not already set :
 * - [MediaMetadata.title] with [Chapter.title]
 * - [MediaMetadata.subtitle] with [Chapter.lead]
 * - [MediaMetadata.description] with [Chapter.description]
 *
 * @property mediaCompositionDataSource The MediaCompositionDataSource to use to load a MediaComposition.
 * @property trackerDataProvider The TrackerDataProvider to customize TrackerData.
 */
class MediaCompositionMediaItemSource(
    private val mediaCompositionDataSource: MediaCompositionDataSource,
    private val trackerDataProvider: TrackerDataProvider? = null
) : MediaItemSource {
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
        require(MediaUrn.isValid(mediaItem.mediaId)) { "Invalid urn=${mediaItem.mediaId}" }
        val mediaUri = mediaItem.localConfiguration?.uri
        require(!MediaUrn.isValid(mediaUri.toString())) { "Uri can't be a urn" }
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
                val trackerData = mediaItem.getMediaItemTrackerData()
                trackerDataProvider?.update(trackerData, resource, chapter, result.data)
                trackerData.putData(SRGEventLoggerTracker::class.java, null)
                getComScoreData(result.data, chapter, resource)?.let {
                    trackerData.putData(ComScoreTracker::class.java, it)
                }
                return mediaItem.buildUpon()
                    .setMediaMetadata(fillMetaData(mediaItem.mediaMetadata, chapter))
                    .setDrmConfiguration(fillDrmConfiguration(resource))
                    .setTrackerData(trackerData)
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

        private fun getComScoreData(mediaComposition: MediaComposition, chapter: Chapter, resource: Resource): ComScoreTracker.Data? {
            val comScoreData = HashMap<String, String>().apply {
                chapter.comScoreAnalyticsLabels?.let {
                    mediaComposition.comScoreAnalyticsLabels?.let { mediaComposition -> putAll(mediaComposition) }
                    putAll(it)
                }
                resource.comScoreAnalyticsLabels?.let { putAll(it) }
            }
            return if (comScoreData.isNotEmpty()) {
                ComScoreTracker.Data(comScoreData)
            } else {
                null
            }
        }
    }
}
