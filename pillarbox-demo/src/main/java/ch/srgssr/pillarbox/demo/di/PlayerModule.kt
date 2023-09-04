/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.di

import android.content.Context
import androidx.media3.exoplayer.DefaultLoadControl
import ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.getVector
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.demo.data.MixedMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import kotlin.time.Duration.Companion.seconds

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
        val seekIncrement = SeekIncrement(backward = 10.seconds, forward = 30.seconds)
        /*
         * Seem to improve load time, very useful for doing smooth seeking
         */
        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(
                2_000, // DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                2_000, // DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                2_000,
                2_000
            )
            .setBackBuffer(2_000, true)
            .build()
        return PillarboxPlayer(
            context = context,
            mediaItemSource = provideMixedItemSource(context),
            /**
             * Optional, only needed if you plan to play akamai token protected content
             */
            dataSourceFactory = AkamaiTokenDataSource.Factory(),
            mediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
            seekIncrement = seekIncrement,
            loadControl = loadControl
        )
    }
}
