/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.containsImageTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * A [ProgressTrackerState] implementation that updates the [Player] progress every time [onChanged] is called.
 *
 * @param player The [Player] whose progress needs to be tracked.
 * @param coroutineScope The [CoroutineScope] used for managing [StateFlow]s.
 * @param imageOutput The [ImageOutput] to render the image track.
 */
class SmoothProgressTrackerState(
    private val player: PillarboxPlayer,
    coroutineScope: CoroutineScope,
    private val imageOutput: ImageOutput = ImageOutput.NO_OP,
) : ProgressTrackerState {
    private var storedSeekParameters = player.getSeekParameters()
    private var storedPlayWhenReady = player.playWhenReady
    private var storedSmoothSeeking = player.smoothSeekingEnabled
    private var storedTrackSelectionParameters = player.trackSelectionParameters
    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
    private var startChanging = false
    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (!player.isSeekParametersAvailable) return
        if (!startChanging) {
            startChanging = true
            storedPlayWhenReady = player.playWhenReady
            storedSmoothSeeking = player.smoothSeekingEnabled
            storedSeekParameters = player.getSeekParameters()
            storedTrackSelectionParameters = player.trackSelectionParameters
            player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
            player.smoothSeekingEnabled = true
            player.playWhenReady = false
            val imageAvailable = player.isImageOutputAvailable && player.currentTracks.containsImageTrack() && imageOutput != ImageOutput.NO_OP
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().apply {
                setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                setTrackTypeDisabled(C.TRACK_TYPE_METADATA, true)
                if (imageAvailable) {
                    setPrioritizeImageOverVideoEnabled(true)
                } else {
                    setTrackTypeDisabled(C.TRACK_TYPE_IMAGE, true)
                }
            }.build()
            if (imageAvailable) {
                player.setImageOutput(imageOutput)
            }
        }
        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        startChanging = false
        simpleProgressTrackerState.onFinished()
        player.trackSelectionParameters = storedTrackSelectionParameters
        player.smoothSeekingEnabled = storedSmoothSeeking
        player.setSeekParameters(storedSeekParameters)
        player.playWhenReady = storedPlayWhenReady
        player.setImageOutput(null)
    }
}
