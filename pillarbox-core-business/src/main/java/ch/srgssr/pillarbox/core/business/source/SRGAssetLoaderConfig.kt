/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.DataSource.Factory
import androidx.media3.datasource.DefaultDataSource
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenProvider
import ch.srgssr.pillarbox.core.business.integrationlayer.ResourceSelector
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers

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
    private var commanderActTrackerFactory: MediaItemTracker.Factory<CommandersActTracker.Data> =
        CommandersActTracker.Factory(SRGAnalytics.commandersAct, Dispatchers.Default)
    private var comscoreTrackerFactory: MediaItemTracker.Factory<ComScoreTracker.Data> = ComScoreTracker.Factory()

    @VisibleForTesting
    internal fun commanderActTrackerFactory(commanderActTrackerFactory: MediaItemTracker.Factory<CommandersActTracker.Data>) {
        this.commanderActTrackerFactory = commanderActTrackerFactory
    }

    @VisibleForTesting
    internal fun comscoreTrackerFactory(comscoreTrackerFactory: MediaItemTracker.Factory<ComScoreTracker.Data>) {
        this.comscoreTrackerFactory = comscoreTrackerFactory
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
            comscoreTrackerFactory = comscoreTrackerFactory,
            mediaCompositionService = mediaCompositionService,
            resourceSelector = ResourceSelector(),
        )
    }
}
