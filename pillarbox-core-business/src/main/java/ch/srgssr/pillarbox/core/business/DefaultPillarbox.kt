/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.DefaultLoadControl
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.integrationlayer.service.DefaultMediaCompositionDataSource
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.data.MediaItemSource
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
     * @param loadControl The load control, bye default [DefaultLoadControl].
     * @return [PillarboxPlayer] suited for SRG.
     */
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        mediaItemTrackerRepository: MediaItemTrackerProvider = DefaultMediaItemTrackerRepository(),
        mediaItemSource: MediaItemSource = MediaCompositionMediaItemSource(
            mediaCompositionDataSource = DefaultMediaCompositionDataSource(),
        ),
        dataSourceFactory: DataSource.Factory = AkamaiTokenDataSource.Factory(),
        loadControl: PillarboxLoadControl = PillarboxLoadControl(),
    ): PillarboxPlayer {
        return PillarboxPlayer(
            context = context,
            seekIncrement = seekIncrement,
            dataSourceFactory = dataSourceFactory,
            mediaItemSource = mediaItemSource,
            mediaItemTrackerProvider = mediaItemTrackerRepository,
            loadControl = loadControl
        )
    }
}
