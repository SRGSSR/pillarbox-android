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
import androidx.media3.datasource.DataSource.Factory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.extension.getBlockReasonExceptionOrNull
import ch.srgssr.pillarbox.core.business.integrationlayer.ResourceSelector
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.ILUrl.Companion.toIlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.network.HttpResultException
import ch.srgssr.pillarbox.player.tracker.FactoryData
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * Creates an [SRGAssetLoader] instance using a DSL-style configuration.
 *
 * **Basic usage**
 *
 * ```kotlin
 * val srgAssetLoader = SRGAssetLoader(context)
 * ```
 *
 * **Custom configuration**
 *
 * ```kotlin
 * val srgAssetLoader = SRGAssetLoader(context) {
 *      mediaCompositionService(CustomMediaCompositionService())
 * }
 * ```
 *
 * @param context The Android [Context] required for the asset loader.
 * @param block A lambda that receives an [SRGAssetLoaderConfig] instance, allowing you to customize the loader's settings.
 *
 * @return A [SRGAssetLoader] instance.
 */
@PillarboxDsl
fun SRGAssetLoader(context: Context, block: SRGAssetLoaderConfig.() -> Unit = {}): SRGAssetLoader {
    return SRGAssetLoaderConfig(context).apply(block).create()
}

/**
 *  Represents the MIME type for SRG SSR content.
 */
const val MimeTypeSrg = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

/**
 * An [AssetLoader] responsible for loading SRG assets.
 *
 * To create an instance of this class, use the [SRGAssetLoader] builder function:
 *
 * **Basic usage**
 *
 * ```kotlin
 * val srgAssetLoader = SRGAssetLoader(context)
 * ```
 *
 * **Custom configuration**
 *
 * ```kotlin
 * val srgAssetLoader = SRGAssetLoader(context) {
 *      mediaCompositionService(CustomMediaCompositionService())
 * }
 * ```
 */
@Suppress("LongParameterList")
class SRGAssetLoader internal constructor(
    akamaiTokenProvider: AkamaiTokenProvider,
    dataSourceFactory: Factory,
    private val mediaCompositionService: MediaCompositionService,
    private val commanderActTrackerFactory: MediaItemTracker.Factory<CommandersActTracker.Data>,
    private val comscoreTrackerFactory: MediaItemTracker.Factory<ComScoreTracker.Data>,
    private val customTrackerData: (MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit)?,
    private val customMediaMetadata: (suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit)?,
    private val resourceSelector: ResourceSelector,
    private val spriteSheetLoader: SpriteSheetLoader,
) : AssetLoader(
    mediaSourceFactory = DefaultMediaSourceFactory(AkamaiTokenDataSource.Factory(akamaiTokenProvider, dataSourceFactory))
) {

    private val defaultMediaMetadata: suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit =
        { resource, chapter, mediaComposition ->
            DefaultMediaMetaDataProvider.invoke(this, resource, chapter, mediaComposition)
            customMediaMetadata?.invoke(this, resource, chapter, mediaComposition)
        }

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        val localConfiguration = mediaItem.localConfiguration ?: return false
        return localConfiguration.mimeType == MimeTypeSrg && kotlin.runCatching {
            localConfiguration.uri.toIlUrl()
        }.isSuccess
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        checkNotNull(mediaItem.localConfiguration)
        val result = mediaCompositionService.fetchMediaComposition(mediaItem.localConfiguration!!.uri).getOrElse {
            when (it) {
                is HttpResultException -> throw it
                is SerializationException -> throw DataParsingException(it)
                else -> throw IOException(it.message)
            }
        }

        val chapter = result.mainChapter
        chapter.getBlockReasonExceptionOrNull()?.let {
            throw it
        }

        val resource = resourceSelector.selectResourceFromChapter(chapter) ?: throw ResourceNotFoundException()
        var uri = Uri.parse(resource.url)
        if (resource.tokenType == Resource.TokenType.AKAMAI) {
            uri = AkamaiTokenDataSource.appendTokenQueryToUri(uri)
        }
        val trackerData = MutableMediaItemTrackerData()
        trackerData[SRGEventLoggerTracker::class.java] = FactoryData(SRGEventLoggerTracker.Factory(), Unit)
        getComScoreData(result, chapter, resource)?.let {
            trackerData[ComScoreTracker::class.java] = FactoryData(comscoreTrackerFactory, it)
        }
        getCommandersActData(result, chapter, resource)?.let {
            trackerData[CommandersActTracker::class.java] = FactoryData(commanderActTrackerFactory, it)
        }
        customTrackerData?.invoke(trackerData, resource, chapter, result)

        val loadingMediaItem = MediaItem.Builder()
            .setDrmConfiguration(fillDrmConfiguration(resource))
            .setUri(uri)
            .build()
        val contentMediaSource = mediaSourceFactory.createMediaSource(loadingMediaItem)
        val mediaSource = chapter.spriteSheet?.let {
            MergingMediaSource(contentMediaSource, SpriteSheetMediaSource(it, loadingMediaItem, spriteSheetLoader))
        } ?: contentMediaSource
        return Asset(
            mediaSource = mediaSource,
            trackersData = trackerData.toMediaItemTrackerData(),
            mediaMetadata = mediaItem.mediaMetadata.buildUpon().apply {
                defaultMediaMetadata.invoke(this, mediaItem.mediaMetadata, chapter, result)
            }.build(),
            blockedTimeRanges = SegmentAdapter.getBlockedTimeRanges(chapter.listSegment),
        )
    }

    private fun fillDrmConfiguration(resource: Resource): MediaItem.DrmConfiguration? {
        val drm = resource.drmList?.find { it.type == Drm.Type.WIDEVINE }
        return drm?.let {
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(it.licenseUrl)
                .setMultiSession(true)
                .build()
        }
    }

    /**
     * ComScore (MediaPulse) doesn't want to track audio. Integration layer doesn't fill analytics labels for audio content,
     * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
     */
    private fun getComScoreData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): ComScoreTracker.Data? {
        val comScoreData = mutableMapOf<String, String>().apply {
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
     * CommandersAct doesn't want to track audio. Integration layer doesn't fill analytics labels for audio content,
     * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
     */
    private fun getCommandersActData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): CommandersActTracker.Data? {
        val commandersActData = mutableMapOf<String, String>().apply {
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
