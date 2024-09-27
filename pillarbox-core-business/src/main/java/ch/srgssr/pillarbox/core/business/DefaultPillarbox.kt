/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxExoPlayer.Companion.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * [DefaultPillarbox] is a convenient class to create a [PillarboxExoPlayer] that suits the default SRG needs.
 */
object DefaultPillarbox {
    private val defaultSeekIncrement = SeekIncrement(backward = 10.seconds, forward = 30.seconds)

    /**
     * Invoke create an instance of [PillarboxExoPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param maxSeekToPreviousPosition The [Player.getMaxSeekToPreviousPosition] value.
     * @param mediaCompositionService The [MediaCompositionService] to use, by default [HttpMediaCompositionService].
     * @param loadControl The load control, by default [PillarboxLoadControl].
     * @return [PillarboxExoPlayer] suited for SRG.
     */
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        maxSeekToPreviousPosition: Duration = DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION,
        mediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
        loadControl: LoadControl = PillarboxLoadControl(),
    ): PillarboxExoPlayer {
        return DefaultPillarbox(
            context = context,
            seekIncrement = seekIncrement,
            maxSeekToPreviousPosition = maxSeekToPreviousPosition,
            mediaCompositionService = mediaCompositionService,
            loadControl = loadControl,
            clock = Clock.DEFAULT,
            coroutineContext = Dispatchers.Default,
        )
    }

    /**
     * Invoke create an instance of [PillarboxExoPlayer]
     *
     * @param context The context.
     * @param seekIncrement The seek increment.
     * @param maxSeekToPreviousPosition The [Player.getMaxSeekToPreviousPosition] value.
     * @param loadControl The load control, by default [DefaultLoadControl].
     * @param mediaCompositionService The [MediaCompositionService] to use, by default [HttpMediaCompositionService].
     * @param clock The internal clock used by the player.
     * @param coroutineContext The coroutine context to use for this player.
     * @return [PillarboxExoPlayer] suited for SRG.
     */
    @VisibleForTesting
    operator fun invoke(
        context: Context,
        seekIncrement: SeekIncrement = defaultSeekIncrement,
        maxSeekToPreviousPosition: Duration = DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION,
        loadControl: LoadControl = DefaultLoadControl(),
        mediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
        clock: Clock,
        coroutineContext: CoroutineContext,
    ): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            seekIncrement = seekIncrement,
            maxSeekToPreviousPosition = maxSeekToPreviousPosition,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(SRGAssetLoader(context, mediaCompositionService))
            },
            loadControl = loadControl,
            clock = clock,
            coroutineContext = coroutineContext,
        )
    }
}
