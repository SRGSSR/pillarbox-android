/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.di

import android.content.Context
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.getVector
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.demo.shared.data.MixedMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory

/**
 * Dependencies to make custom Dependency Injection
 */
object PlayerModule {

    private fun provideIntegrationLayerItemSource(context: Context): MediaCompositionMediaItemSource =
        MediaCompositionMediaItemSource(DefaultMediaCompositionDataSource(vector = context.getVector()))

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
        val builder = DefaultPillarbox.Builder(context)
        builder.setMediaSourceFactory(
            PillarboxMediaSourceFactory(
                mediaItemSource = provideMixedItemSource(context),
                defaultMediaSourceFactory = DefaultMediaSourceFactory(AkamaiTokenDataSource.Factory())
            )
        )
        return PillarboxPlayer(builder, DefaultMediaItemTrackerRepository())
    }
}
