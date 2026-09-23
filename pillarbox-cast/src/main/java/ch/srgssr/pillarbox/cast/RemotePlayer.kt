/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.textTracks
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * A player that handle switching from local to remote playback.
 *
 * @param localPlayer The [PillarboxPlayer] used when not remotely casting.
 * @param castPlayer The [PillarboxCastPlayer] used when Cast session is available.
 * @param synchronizer The [PlayerSynchronizer] to synchronize state when transitioning between local to remote playback.
 */
class RemotePlayer(
    private val localPlayer: PillarboxExoPlayer,
    private val castPlayer: PillarboxCastPlayer,
    private val synchronizer: PlayerSynchronizer = DefaultPlayerSynchronizer(),
) : ForwardingSimpleBasePlayer(if (castPlayer.isCastSessionAvailable()) castPlayer else localPlayer), PillarboxPlayer {

    private var player: PillarboxPlayer
        get() = super.player as PillarboxPlayer
        set(value) {
            super.player = value
        }

    override var trackingEnabled: Boolean
        get() = player.trackingEnabled
        set(value) {
            player.trackingEnabled = value
        }
    override val isImageOutputAvailable: Boolean
        get() = player.isImageOutputAvailable

    override val isMetricsAvailable: Boolean
        get() = player.isMetricsAvailable

    override val currentPillarboxMetadata: PillarboxMetadata
        get() = player.currentPillarboxMetadata

    // TODO MBO check
    /**
     * Smooth seeking is not supported on [CastPlayer]. By its very nature (ie. being remote), seeking **smoothly** is impossible to achieve.
     */
    override var smoothSeekingEnabled: Boolean = false
        set(value) {}

    /**
     * [CastPlayer] does not support [SeekParameters].
     */
    override val isSeekParametersAvailable: Boolean = false
    // TODO MBO check end

    private val sessionListener = object : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            updateActivePlayer(castPlayer)
        }

        override fun onCastSessionUnavailable() {
            updateActivePlayer(localPlayer)
        }
    }

    private val pillarboxPlayerListeners = LinkedHashSet<PillarboxPlayer.Listener>()

    init {
        castPlayer.setSessionAvailabilityListener(sessionListener)
    }

    override fun setImageOutput(imageOutput: ImageOutput?) {
        player.setImageOutput(imageOutput)
    }

    override fun setScrubbingModeEnabled(scrubbingModeEnabled: Boolean) {
        player.setScrubbingModeEnabled(scrubbingModeEnabled)
    }

    override fun isScrubbingModeEnabled(): Boolean {
        return player.isScrubbingModeEnabled()
    }

    override fun addListener(listener: PillarboxPlayer.Listener) {
        pillarboxPlayerListeners.add(listener)
        player.addListener(listener)
    }

    override fun removeListener(listener: PillarboxPlayer.Listener) {
        pillarboxPlayerListeners.remove(listener)
        player.removeListener(listener)
    }

    override fun handleRelease(): ListenableFuture<*> {
        castPlayer.release()
        castPlayer.setSessionAvailabilityListener(null)
        localPlayer.release()
        return Futures.immediateVoidFuture()
    }

    // TODO MBO check start
    override fun setSeekParameters(seekParameters: SeekParameters?) = Unit

    override fun getSeekParameters(): SeekParameters {
        return SeekParameters.DEFAULT
    }

    // TODO MBO check end

    private fun updateTrackSelection(previousPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer) {
        val selectedAudioTrack = previousPlayer.currentTracks.audioTracks.firstOrNull { it.isSelected }
        val selectedTextTrack = previousPlayer.currentTracks.textTracks.firstOrNull { it.isSelected }
        newPlayer.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                if (!tracks.isEmpty) {
                    val trackSelection =
                        synchronizer.onTracksChanged(
                            newTracks = tracks,
                            selectedAudioTrack = selectedAudioTrack,
                            selectedTextTrack = selectedTextTrack
                        )
                    val newSelectedAudioTrack = trackSelection.audioTrack
                    val newSelectedTextTrack = trackSelection.textTrack
                    newSelectedAudioTrack?.let {
                        newPlayer.selectTrack(it)
                    }
                    newSelectedTextTrack?.let {
                        newPlayer.selectTrack(it)
                    }
                    newPlayer.removeListener(this)
                }
            }
        })
    }

    private fun updateActivePlayer(newPlayer: PillarboxPlayer) {
        val previousPlayer = player
        if (newPlayer == previousPlayer) return

        updateTrackSelection(previousPlayer, newPlayer)
        synchronizer.onPlayerChanged(previousPlayer, newPlayer)

        pillarboxPlayerListeners.toList().forEach {
            previousPlayer.removeListener(it)
            newPlayer.addListener(it)
        }

        if (previousPlayer.playbackState != STATE_IDLE) {
            newPlayer.prepare()
        }

        previousPlayer.stop()
        player = newPlayer
    }
}
