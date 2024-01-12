/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
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

    private var storedPlaybackSpeed = player.getPlaybackSpeed()
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
                .setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                // .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                // .setOverrideForType(TrackSelectionOverride(player.currentTracks.video.first().mediaTrackGroup, 13))
                .build()
            loadControl.trickModeEnabled = true
            // player.setPlaybackSpeed(SEEKING_PLAYBACK_SPEED)
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        player.smoothSeekingEnabled = false
        player.playWhenReady = storedPlayWhenReady
        player.trackSelectionParameters = storedTrackSelectionParameters
        player.setPlaybackSpeed(storedPlaybackSpeed)
        startChanging = false
        loadControl.trickModeEnabled = false
        simpleProgressTrackerState.onFinished()
    }

    private companion object {
        /*
         * For Dash DRM produce by the SRG, speed at 100 and more with disabled audio track and TrickPlayLoadControl work crazy good.
         * But HLS stream lesser.
         */
        private const val SEEKING_PLAYBACK_SPEED = 10f
    }
}
