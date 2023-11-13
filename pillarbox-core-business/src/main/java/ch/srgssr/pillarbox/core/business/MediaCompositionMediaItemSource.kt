/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.images.ImageScalingService
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageFormat
import ch.srgssr.pillarbox.core.business.images.ImageScalingService.ImageWidth
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.SerializationException
import java.io.IOException

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
 * @property imageScalingService The ImageScaleService to use to get a scaled image.
 * @property trackerDataProvider The TrackerDataProvider to customize TrackerData.
 */
class MediaCompositionMediaItemSource(
    private val mediaCompositionDataSource: MediaCompositionDataSource,
    private val imageScalingService: ImageScalingService,
    private val trackerDataProvider: TrackerDataProvider? = null
) : MediaItemSource {
    private val resourceSelector = ResourceSelector()

    private fun fillMetaData(metadata: MediaMetadata, chapter: Chapter): MediaMetadata {
        val builder = metadata.buildUpon()
        metadata.title ?: builder.setTitle(chapter.title)
        metadata.subtitle ?: builder.setSubtitle(chapter.lead)
        metadata.description ?: builder.setDescription(chapter.description)
        metadata.artworkUri ?: run {
            val artworkUri = imageScalingService.getScaledImageUrl(
                imageUrl = chapter.imageUrl,
                width = ImageWidth.W480,
                format = ImageFormat.WEBP
            ).toUri()

            builder.setArtworkUri(artworkUri)
        }
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
        val result = mediaCompositionDataSource.getMediaCompositionByUrn(mediaItem.mediaId).getOrElse {
            when (it) {
                is ClientRequestException -> {
                    throw HttpResultException(it)
                }

                is SerializationException -> {
                    throw DataParsingException(it)
                }

                else -> {
                    throw IOException(it.message)
                }
            }
        }
        val chapter = result.mainChapter
        chapter.blockReason?.let {
            throw BlockReasonException(it)
        }
        chapter.listSegment?.firstNotNullOfOrNull { it.blockReason }?.let {
            throw BlockReasonException(it)
        }

        val resource = resourceSelector.selectResourceFromChapter(chapter) ?: throw ResourceNotFoundException()
        var uri = Uri.parse(resource.url)
        if (resource.tokenType == Resource.TokenType.AKAMAI) {
            uri = appendTokenQueryToUri(uri)
        }
        val trackerData = mediaItem.getMediaItemTrackerData()
        trackerDataProvider?.update(trackerData, resource, chapter, result)
        trackerData.putData(SRGEventLoggerTracker::class.java, null)
        getComScoreData(result, chapter, resource)?.let {
            trackerData.putData(ComScoreTracker::class.java, it)
        }
        getCommandersActData(result, chapter, resource)?.let {
            trackerData.putData(CommandersActTracker::class.java, it)
        }
        return mediaItem.buildUpon()
            .setMediaMetadata(fillMetaData(mediaItem.mediaMetadata, chapter))
            .setDrmConfiguration(fillDrmConfiguration(resource))
            .setTrackerData(trackerData)
            .setUri(uri)
            .build()
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

        /**
         * ComScore (MediaPulse) don't want to track audio. Integration layer doesn't fill analytics labels for audio content,
         * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
         */
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

        /**
         * ComScore (MediaPulse) don't want to track audio. Integration layer doesn't fill analytics labels for audio content,
         * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
         */
        private fun getCommandersActData(mediaComposition: MediaComposition, chapter: Chapter, resource: Resource): CommandersActTracker.Data? {
            val commandersActData = HashMap<String, String>().apply {
                mediaComposition.analyticsLabels?.let { mediaComposition -> putAll(mediaComposition) }
                chapter.analyticsLabels?.let { putAll(it) }
                resource.analyticsLabels?.let { putAll(it) }
            }
            return if (commandersActData.isNotEmpty()) {
                // TODO : sourceId can be store inside MediaItem.metadata.extras["source_key"]
                CommandersActTracker.Data(assets = commandersActData, sourceId = null)
            } else {
                null
            }
        }
    }
}
