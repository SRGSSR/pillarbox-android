/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.core.business.HttpResultException
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.ResourceSelector
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * Mime Type for representing SRG SSR content
 */
const val MimeTypeSrg = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

/**
 * SRG SSR implementation of [AssetLoader].
 *
 * @param context The context.
 * @param mediaCompositionService The service to load a [MediaComposition].
 */
class SRGAssetLoader(
    context: Context,
    private val mediaCompositionService: MediaCompositionService = HttpMediaCompositionService()
) : AssetLoader(
    mediaSourceFactory = DefaultMediaSourceFactory(AkamaiTokenDataSource.Factory(AkamaiTokenProvider(), DefaultDataSource.Factory(context)))
) {
    /**
     * An interface to customize how [SRGAssetLoader] should fill [MediaMetadata].
     */
    fun interface MediaMetadataProvider {
        /**
         * Provide
         *
         * @param mediaMetadataBuilder The [MediaMetadata.Builder] used to build MediaMetadata.
         * @param resource The [Resource] the player will play.
         * @param chapter The main [Chapter] from the mediaComposition
         * @param mediaComposition The [MediaComposition] loaded from [MediaCompositionService].
         */
        fun provide(
            mediaMetadataBuilder: MediaMetadata.Builder,
            resource: Resource,
            chapter: Chapter,
            mediaComposition: MediaComposition
        )
    }

    /**
     * An interface to add custom tracker data.
     */
    fun interface TrackerDataProvider {
        /**
         * Provide Tracker Data to the [Asset]. The official SRG trackers are always setup by [SRGAssetLoader].
         *
         * @param trackerDataBuilder The [MediaItemTrackerData.Builder] to add trackers data.
         * @param resource The [Resource] the player will play.
         * @param chapter The main [Chapter] from the mediaComposition
         * @param mediaComposition The [MediaComposition] loaded from [MediaCompositionService].
         */
        fun provide(
            trackerDataBuilder: MediaItemTrackerData.Builder,
            resource: Resource,
            chapter: Chapter,
            mediaComposition: MediaComposition
        )
    }

    private val resourceSelector = ResourceSelector()

    /**
     * Media metadata provider to customize [Asset.mediaMetadata].
     */
    var mediaMetadataProvider: MediaMetadataProvider = DefaultMediaMetaDataProvider()

    /**
     * Tracker data provider to customize [Asset.trackersData].
     */
    var trackerDataProvider: TrackerDataProvider? = null

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration?.mimeType == MimeTypeSrg || mediaItem.localConfiguration?.uri?.lastPathSegment.isValidMediaUrn()
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        checkNotNull(mediaItem.localConfiguration)
        val result = mediaCompositionService.fetchMediaComposition(mediaItem.localConfiguration!!.uri).getOrElse {
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
            uri = AkamaiTokenDataSource.appendTokenQueryToUri(uri)
        }
        val trackerData = mediaItem.getMediaItemTrackerData().buildUpon().apply {
            trackerDataProvider?.provide(this, resource, chapter, result)
            putData(SRGEventLoggerTracker::class.java)
            getComScoreData(result, chapter, resource)?.let {
                putData(ComScoreTracker::class.java, it)
            }
            getCommandersActData(result, chapter, resource)?.let {
                putData(CommandersActTracker::class.java, it)
            }
        }.build()

        val loadingMediaItem = MediaItem.Builder()
            .setDrmConfiguration(fillDrmConfiguration(resource))
            .setUri(uri)
            .build()
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(loadingMediaItem),
            trackersData = trackerData,
            mediaMetadata = mediaItem.mediaMetadata.buildUpon().apply {
                mediaMetadataProvider.provide(
                    this,
                    chapter = chapter,
                    resource = resource,
                    mediaComposition = result,
                )
            }.build()
        )
    }

    private fun fillDrmConfiguration(resource: Resource): MediaItem.DrmConfiguration? {
        val drm = resource.drmList.orEmpty().find { it.type == Drm.Type.WIDEVINE }
        return drm?.let {
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(it.licenseUrl)
                .build()
        }
    }

    /**
     * ComScore (MediaPulse) don't want to track audio. Integration layer doesn't fill analytics labels for audio content,
     * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
     */
    private fun getComScoreData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): ComScoreTracker.Data? {
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
    private fun getCommandersActData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): CommandersActTracker.Data? {
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
