/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.DataSource.Factory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider
import ch.srgssr.pillarbox.core.business.integrationlayer.ResourceSelector
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers

/**
 * Configuration class for [SRGAssetLoader].
 *
 * This class allows you to customize the behavior of the asset loader, such as:
 *
 * - Providing a custom data source factory.
 * - Specifying a media composition service.
 * - Setting an HTTP client for network requests.
 * - Injecting custom data into media item tracker data.
 * - Overriding the default media metadata.
 * - Providing a custom [Bitmap] loader for sprite sheet.
 *
 * @param context The Android [Context].
 */
@PillarboxDsl
class SRGAssetLoaderConfig internal constructor(context: Context) {
    private var dataSourceFactory: Factory = DefaultDataSource.Factory(context, OkHttpDataSource.Factory(PillarboxOkHttp()))
    private var mediaCompositionService: MediaCompositionService = HttpMediaCompositionService()
    private var akamaiTokenProvider = AkamaiTokenProvider()
    private var mediaItemTrackerDataConfig: (MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit)? = null
    private var mediaMetadataOverride: (suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit)? = null
    private var commanderActTrackerFactory: MediaItemTracker.Factory<CommandersActTracker.Data> =
        CommandersActTracker.Factory(SRGAnalytics.commandersAct, Dispatchers.Default)
    private var comscoreTrackerFactory: MediaItemTracker.Factory<ComScoreTracker.Data> = ComScoreTracker.Factory()
    private var spriteSheetLoader: SpriteSheetLoader? = SpriteSheetLoader.Default()

    @VisibleForTesting
    internal fun commanderActTrackerFactory(commanderActTrackerFactory: MediaItemTracker.Factory<CommandersActTracker.Data>) {
        this.commanderActTrackerFactory = commanderActTrackerFactory
    }

    @VisibleForTesting
    internal fun comscoreTrackerFactory(comscoreTrackerFactory: MediaItemTracker.Factory<ComScoreTracker.Data>) {
        this.comscoreTrackerFactory = comscoreTrackerFactory
    }

    /**
     * Sets the data source factory.
     *
     * @param dataSourceFactory The data source factory.
     */
    fun dataSourceFactory(dataSourceFactory: Factory) {
        this.dataSourceFactory = dataSourceFactory
    }

    /**
     * Sets the media composition service.
     *
     * @param mediaCompositionService The media composition service.
     */
    fun mediaCompositionService(mediaCompositionService: MediaCompositionService) {
        this.mediaCompositionService = mediaCompositionService
    }

    /**
     * Sets the HTTP client used by the [MediaCompositionService] and [AkamaiTokenProvider].
     *
     * Note that this will override any existing [MediaCompositionService] set using [mediaCompositionService].
     *
     * @param httpClient The HTTP client.
     */
    fun httpClient(httpClient: HttpClient) {
        mediaCompositionService = HttpMediaCompositionService(httpClient)
        akamaiTokenProvider = AkamaiTokenProvider(httpClient)
    }

    /**
     * Configures a block to inject custom data into the [MutableMediaItemTrackerData] used for tracking media playback.
     *
     * The provided block will be executed when creating the tracker data, giving you access to:
     *
     * - **`this`**: the [MutableMediaItemTrackerData] instance being configured.
     * - **`Resource`**: the [Resource] being tracked.
     * - **`Chapter`**: the [Chapter] being tracked.
     * - **`MediaComposition`**: the current [MediaComposition].
     *
     * **Example**
     *
     * ```kotlin
     * val srgAssetLoader = SRGAssetLoader(context) {
     *     trackerData { resource, chapter, mediaComposition ->
     *         this["event-logger"] = FactoryData(SRGEventLoggerTracker.Factory(), Unit)
     *     }
     * }
     * ```
     *
     * @param block The configuration block to execute.
     */
    fun trackerData(block: MutableMediaItemTrackerData.(Resource, Chapter, MediaComposition) -> Unit) {
        mediaItemTrackerDataConfig = block
    }

    /**
     * Configures the [MediaMetadata] that is created for the loaded asset.
     *
     * The provided block will be executed when creating the asset's metadata, giving you access to:
     *
     * - **`MediaMetadata`**: the [MediaMetadata] instance.
     * - **`Chapter`**: the current [Chapter].
     * - **`MediaComposition`**: the current [MediaComposition].
     *
     * **Example**
     *
     * ```kotlin
     * val srgAssetLoader = SRGAssetLoader(context) {
     *     mediaMetaData { mediaMetadata, chapter, mediaComposition ->
     *         setTitle(chapter.title)
     *     }
     * }
     * ```
     *
     * @param block The configuration block to execute.
     */
    fun mediaMetaData(block: suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit) {
        mediaMetadataOverride = block
    }

    /**
     * Sets the [SpriteSheetLoader] to be used to load a [Bitmap] from a [SpriteSheet].
     *
     * **Example**
     *
     * ```kotlin
     * val srgAssetLoader = SRGAssetLoader(context) {
     *     spriteSheetLoader { spriteSheet, onComplete ->
     *         onComplete(loadBitmap(spriteSheet.url))
     *     }
     * }
     * ```
     *
     * @param spriteSheetLoader The [SpriteSheetLoader] instance to use.
     */
    fun spriteSheetLoader(spriteSheetLoader: SpriteSheetLoader?) {
        this.spriteSheetLoader = spriteSheetLoader
    }

    internal fun create(): SRGAssetLoader {
        return SRGAssetLoader(
            akamaiTokenProvider = akamaiTokenProvider,
            dataSourceFactory = dataSourceFactory,
            customTrackerData = mediaItemTrackerDataConfig,
            customMediaMetadata = mediaMetadataOverride,
            commanderActTrackerFactory = commanderActTrackerFactory,
            comscoreTrackerFactory = comscoreTrackerFactory,
            mediaCompositionService = mediaCompositionService,
            resourceSelector = ResourceSelector(),
            spriteSheetLoader = spriteSheetLoader
        )
    }
}
