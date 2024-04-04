/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import kotlin.time.Duration.Companion.seconds

/**
 * DefaultPillarbox convenient class to create [PillarboxExoPlayer] that suit Default SRG needs.
 */
object DefaultPillarbox {
    private val defaultSeekIncrement = SeekIncrement(backward = 10.seconds, forward = 30.seconds)

    /**
     * Invoke create an instance of [PillarboxExoPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param mediaItemTrackerRepository The provider of MediaItemTracker, by default [DefaultMediaItemTrackerRepository].
     * @param mediaCompositionService The [MediaCompositionService] to use, by default [HttpMediaCompositionService].
     * @param loadControl The load control, by default [PillarboxLoadControl].
     * @return [PillarboxExoPlayer] suited for SRG.
     */
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        mediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
        loadControl: LoadControl = PillarboxLoadControl(),
    ): PillarboxExoPlayer {
        return DefaultPillarbox(
            context = context,
            seekIncrement = seekIncrement,
            mediaItemTrackerRepository = mediaItemTrackerRepository,
            mediaCompositionService = mediaCompositionService,
            loadControl = loadControl,
            clock = Clock.DEFAULT,
        )
    }

    /**
     * Invoke create an instance of [PillarboxExoPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param mediaItemTrackerRepository The provider of MediaItemTracker, by default [DefaultMediaItemTrackerRepository].
     * @param loadControl The load control, by default [DefaultLoadControl].
     * @param mediaCompositionService The [MediaCompositionService] to use, by default [HttpMediaCompositionService].
     * @param clock The internal clock used by the player.
     * @return [PillarboxExoPlayer] suited for SRG.
     */
    @VisibleForTesting
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        loadControl: LoadControl = DefaultLoadControl(),
        mediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
        clock: Clock,
    ): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            seekIncrement = seekIncrement,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(SRGAssetLoader(context, mediaCompositionService))
            },
            mediaItemTrackerProvider = mediaItemTrackerRepository,
            loadControl = loadControl,
            clock = clock,
        )
    }
}
