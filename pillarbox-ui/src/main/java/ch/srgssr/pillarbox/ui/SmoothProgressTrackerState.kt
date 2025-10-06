/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Player
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
    private var storedTrackSelectionParameters = player.trackSelectionParameters
    private val simpleProgressTrackerState = SimpleProgressTrackerState(player, coroutineScope)
    private var startChanging = false
    override val progress: StateFlow<Duration> = simpleProgressTrackerState.progress

    override fun onChanged(progress: Duration) {
        simpleProgressTrackerState.onChanged(progress)
        if (player.deviceInfo.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE) return
        if (!startChanging) {
            startChanging = true
            storedTrackSelectionParameters = player.trackSelectionParameters
            val imageAvailable = player.isImageOutputAvailable && player.currentTracks.containsImageTrack() && imageOutput != ImageOutput.NO_OP
            if (imageAvailable) {
                player.trackSelectionParameters = storedTrackSelectionParameters.buildUpon()
                    .apply {
                        setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT, C.TRACK_TYPE_AUDIO))
                        setPrioritizeImageOverVideoEnabled(true)
                    }
                    .build()
                player.setImageOutput(imageOutput)
            } else {
                player.trackSelectionParameters = storedTrackSelectionParameters.buildUpon()
                    .apply {
                        setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                    }
                    .build()
                player.setScrubbingModeEnabled(true)
            }
        }

        player.seekTo(progress.inWholeMilliseconds)
    }

    override fun onFinished() {
        startChanging = false
        simpleProgressTrackerState.onFinished()
        if (player.deviceInfo.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE) return
        if (player.isScrubbingModeEnabled()) {
            player.setScrubbingModeEnabled(false)
        }
        player.setImageOutput(null)
        player.trackSelectionParameters = storedTrackSelectionParameters
    }
}
