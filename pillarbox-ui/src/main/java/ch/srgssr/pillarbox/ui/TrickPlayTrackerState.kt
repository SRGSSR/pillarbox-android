/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Experimental TrickPlay [ProgressTrackerState]
 */
class TrickPlayTrackerState(
    private val player: PillarboxExoPlayer,
    coroutineScope: CoroutineScope
) : ProgressTrackerState {

    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
    private var startChanging = false

    private var storedPlayWhenReady = player.playWhenReady
    private var storedTrackSelectionParameters = player.trackSelectionParameters
    private val loadControl = (player as PillarboxPlayer).loadControl

    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (!startChanging) {
            startChanging = true
            storedPlayWhenReady = player.playWhenReady
            storedTrackSelectionParameters = player.trackSelectionParameters
            player.smoothSeekingEnabled = true
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
            player.playWhenReady = false
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
                .build()
            loadControl.trickModeEnabled = true
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        player.smoothSeekingEnabled = false
        player.playWhenReady = storedPlayWhenReady
        player.trackSelectionParameters = storedTrackSelectionParameters

        loadControl.trickModeEnabled = false
        if (startChanging) {
            startChanging = false
            simpleProgressTrackerState.onFinished()
        }
    }
}
