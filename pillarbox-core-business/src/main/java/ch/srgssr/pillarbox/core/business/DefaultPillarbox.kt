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
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.source.SRGMediaSource
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import kotlin.time.Duration.Companion.seconds

/**
 * DefaultPillarbox convenient class to create [PillarboxPlayer] that suit Default SRG needs.
 */
object DefaultPillarbox {
    private val defaultSeekIncrement = SeekIncrement(backward = 10.seconds, forward = 30.seconds)

    /**
     * Invoke create an instance of [PillarboxPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param mediaItemTrackerRepository The provider of MediaItemTracker, by default [DefaultMediaItemTrackerRepository].
     * @param mediaItemSource The MediaItem source by default [MediaCompositionMediaItemSource].
     * @param dataSourceFactory The Http exoplayer data source factory, by default [AkamaiTokenDataSource.Factory].
     * @param loadControl The load control, by default [DefaultLoadControl].
     * @return [PillarboxPlayer] suited for SRG.
     */
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        loadControl: LoadControl = PillarboxLoadControl(),
    ): PillarboxPlayer {
        return DefaultPillarbox(
            context = context,
            seekIncrement = seekIncrement,
            mediaItemTrackerRepository = mediaItemTrackerRepository,
            loadControl = loadControl,
            clock = Clock.DEFAULT,
        )
    }

    /**
     * Invoke create an instance of [PillarboxPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param mediaItemTrackerRepository The provider of MediaItemTracker, by default [DefaultMediaItemTrackerRepository].
     * @param loadControl The load control, by default [DefaultLoadControl].
     * @param clock The internal clock used by the player.
     * @return [PillarboxPlayer] suited for SRG.
     */
    @VisibleForTesting
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        loadControl: LoadControl = PillarboxLoadControl(),
        clock: Clock,
    ): PillarboxPlayer {
        return PillarboxPlayer(
            context = context,
            seekIncrement = seekIncrement,
            mediaSourceFactory = SRGMediaSource.Factory(context),
            mediaItemTrackerProvider = mediaItemTrackerRepository,
            loadControl = loadControl,
            clock = clock,
        )
    }
}
