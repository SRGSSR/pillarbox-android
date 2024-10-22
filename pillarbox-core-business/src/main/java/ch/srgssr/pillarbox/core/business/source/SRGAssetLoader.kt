/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DataSource.Factory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.core.business.HttpResultException
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
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.dsl.PillarboxDsl
import ch.srgssr.pillarbox.player.tracker.FactoryData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * SRG asset loader
 *
 * @param context The [Context].
 * @param block The block to configure [SRGAssetLoader].
 * @receiver [SRGAssetLoaderConfig].
 * @return The configured [SRGAssetLoader].
 */
@PillarboxDsl
fun SRGAssetLoader(context: Context, block: SRGAssetLoaderConfig.() -> Unit = {}): SRGAssetLoader {
    return SRGAssetLoaderConfig(context).apply(block).create()
}

/**
 * Configures [SRGAssetLoader].
 * @param context The context.
 */
@PillarboxDsl
class SRGAssetLoaderConfig internal constructor(context: Context) {
    private var dataSourceFactory: Factory = DefaultDataSource.Factory(context)
    private var mediaCompositionService: MediaCompositionService = HttpMediaCompositionService()
    private var akamaiTokenProvider = AkamaiTokenProvider()
    private var mediaItemTrackerDataConfig: (MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit)? = null
    private var mediaMetadataOverride: (suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit)? = null
    private var commanderActTrackerFactory = CommandersActTracker.Factory(SRGAnalytics.commandersAct, Dispatchers.Default)

    @VisibleForTesting
    internal fun commanderActTrackerFactory(commanderActTrackerFactory: CommandersActTracker.Factory) {
        this.commanderActTrackerFactory = commanderActTrackerFactory
    }

    /**
     * Data source factory
     *
     * @param dataSourceFactory
     */
    fun dataSourceFactory(dataSourceFactory: Factory) {
        this.dataSourceFactory = dataSourceFactory
    }

    /**
     * Media composition service
     *
     * @param mediaCompositionService
     */
    fun mediaCompositionService(mediaCompositionService: MediaCompositionService) {
        this.mediaCompositionService = mediaCompositionService
    }

    /**
     * Http client
     *
     * @param httpClient
     */
    fun httpClient(httpClient: HttpClient) {
        httpMediaCompositionService(httpClient)
        akamaiTokenProvider(httpClient)
    }

    private fun httpMediaCompositionService(httpClient: HttpClient) {
        this.mediaCompositionService = HttpMediaCompositionService(httpClient)
    }

    private fun akamaiTokenProvider(httpClient: HttpClient) {
        this.akamaiTokenProvider = AkamaiTokenProvider(httpClient)
    }

    /**
     * Allow to inject custom data into [MutableMediaItemTrackerData].
     *
     * @param block The block to configure.
     * @receiver [MutableMediaItemTrackerData].
     */
    fun trackerData(block: MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit) {
        mediaItemTrackerDataConfig = block
    }

    /**
     * Override [MediaMetadata] created by default.
     *
     * @param block The block.
     * @receiver [MediaMetadata.Builder].
     */
    fun mediaMetaData(block: suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit) {
        mediaMetadataOverride = block
    }

    internal fun create(): SRGAssetLoader {
        return SRGAssetLoader(
            akamaiTokenProvider = akamaiTokenProvider,
            dataSourceFactory = dataSourceFactory,
            customTrackerData = mediaItemTrackerDataConfig,
            customMediaMetadata = mediaMetadataOverride,
            commanderActTrackerFactory = commanderActTrackerFactory,
            mediaCompositionService = mediaCompositionService,
            resourceSelector = ResourceSelector(),
        )
    }
}

/**
 * Mime Type for representing SRG SSR content
 */
const val MimeTypeSrg = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

/**
 * SRG SSR implementation of an [AssetLoader].
 * @param akamaiTokenProvider The [AkamaiTokenProvider] to use with [AkamaiTokenDataSource].
 * @param dataSourceFactory The data source factory to use with [DefaultMediaSourceFactory].
 * @param mediaCompositionService The service to load a [MediaComposition].
 * @param commanderActTrackerFactory The CommandersAct implementation to use with [CommandersActTracker].
 * @param customTrackerData The block to configure [MutableMediaItemTrackerData].
 * @param customMediaMetadata The block to configure [MediaMetadata].
 * @param resourceSelector The [ResourceSelector].
 */
@Suppress("LongParameterList")
class SRGAssetLoader internal constructor(
    akamaiTokenProvider: AkamaiTokenProvider,
    dataSourceFactory: Factory,
    private val mediaCompositionService: MediaCompositionService,
    private val commanderActTrackerFactory: CommandersActTracker.Factory,
    private val customTrackerData: (MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit)?,
    private val customMediaMetadata: (suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit)?,
    private val resourceSelector: ResourceSelector,
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

        return localConfiguration.mimeType == MimeTypeSrg || localConfiguration.uri.lastPathSegment.isValidMediaUrn()
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
            trackerData[ComScoreTracker::class.java] = FactoryData(ComScoreTracker.Factory(), it)
        }
        getCommandersActData(result, chapter, resource)?.let {
            trackerData[CommandersActTracker::class.java] = FactoryData(commanderActTrackerFactory, it)
        }
        customTrackerData?.invoke(trackerData, resource, chapter, result)

        val loadingMediaItem = MediaItem.Builder()
            .setDrmConfiguration(fillDrmConfiguration(resource))
            .setUri(uri)
            .build()
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(loadingMediaItem),
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
