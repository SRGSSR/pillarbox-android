/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.getVector
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.extension.setPreferredAudioRoleFlagsToAccessibilityManagerSettings
import ch.srgssr.pillarbox.player.extension.setSeekIncrements
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import kotlin.time.Duration.Companion.seconds

/**
 * DefaultPillarbox convenient class to create [PillarboxPlayer] that suit SRG needs.
 */
object DefaultPillarbox {
    private val defaultSeekIncrement = SeekIncrement(backward = 10.seconds, forward = 30.seconds)

    /**
     * Invoke create an instance of [PillarboxPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param mediaItemTrackerRepository The provider of MediaItemTracker.
     * @param trackerProvider The [TrackerDataProvider] to customize tracker data.
     * @return [PillarboxPlayer] suited for SRG.
     */
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        trackerProvider: TrackerDataProvider? = null
    ): PillarboxPlayer {
        return PillarboxPlayer(
            Builder(context, seekIncrement, trackerProvider),
            mediaItemTrackerProvider = mediaItemTrackerRepository
        )
    }

    /**
     * Builder convenient class to create a [ExoPlayer.Builder] that suit SRG needs.
     */
    object Builder {
        /**
         * Invoke
         *
         * @param context The context.
         * @param seekIncrement The seek increment.
         * @param trackerProvider The [TrackerDataProvider] to customize tracker data.
         * @return [ExoPlayer.Builder] suited for SRG.
         */
        operator fun invoke(
            context: Context,
            seekIncrement: SeekIncrement = defaultSeekIncrement,
            trackerProvider: TrackerDataProvider? = null
        ): ExoPlayer.Builder {
            return ExoPlayer.Builder(context)
                .setUsePlatformDiagnostics(false)
                .setSeekIncrements(seekIncrement)
                .setRenderersFactory(
                    DefaultRenderersFactory(context)
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                        .setEnableDecoderFallback(true)
                )
                .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
                .setLoadControl(DefaultLoadControl())
                .setMediaSourceFactory(
                    PillarboxMediaSourceFactory(
                        mediaItemSource = MediaCompositionMediaItemSource(
                            DefaultMediaCompositionDataSource(vector = context.getVector()),
                            trackerProvider
                        ),
                        defaultMediaSourceFactory = DefaultMediaSourceFactory(AkamaiTokenDataSource.Factory())
                    )
                )
                .setTrackSelector(
                    DefaultTrackSelector(
                        context,
                        TrackSelectionParameters.Builder(context)
                            .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
                            .build()
                    )
                )
                .setDeviceVolumeControlEnabled(true) // allow player to control device volume
        }
    }
}
