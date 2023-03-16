/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import android.content.Context
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srg.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemMediaItemTrackerRepository

/**
 * Dependencies to make custom Dependency Injection
 */
object Dependencies {

    private fun provideIntegrationLayerItemSource(context: Context): MediaCompositionMediaItemSource =
        MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(context, IlHost.PROD))

    /**
     * Provide mixed item source that load Url and Urn
     */
    fun provideMixedItemSource(context: Context): MixedMediaItemSource = MixedMediaItemSource(
        provideIntegrationLayerItemSource(context)
    )

    /**
     * Provide default player that allow to play urls and urns content from the SRG
     */
    fun provideDefaultPlayer(context: Context): PillarboxPlayer {
        return PillarboxPlayer(
            context = context,
            mediaItemSource = provideMixedItemSource(context),
            /**
             * Optional, only needed if you plan to play akamai token protected content
             */
            dataSourceFactory = AkamaiTokenDataSource.Factory(),
            mediaItemTrackerProvider = MediaItemMediaItemTrackerRepository().apply {
                registerFactory(SRGEventLoggerTracker::class.java, SRGEventLoggerTracker.Factory())
            }
        )
    }
}
