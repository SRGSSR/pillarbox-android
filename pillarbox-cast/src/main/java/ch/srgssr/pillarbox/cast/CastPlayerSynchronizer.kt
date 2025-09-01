/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.Player
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.textTracks
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Synchronizes a [PillarboxCastPlayer] with a [PillarboxExoPlayer].
 * It switches the two players when the cast session becomes available
 *
 * @param castContext The [CastContext] to use to check if a cast session is available.
 * @param coroutineScope The [CoroutineScope] to use to launch coroutines.
 * @param castPlayer The [PillarboxCastPlayer] to use when a cast session is connected.
 * @param localPlayer The [PillarboxExoPlayer] to use when playing locally.
 * @param playerSynchronizer The [PlayerSynchronizer] to customize the synchronization of the two players.
 */
class CastPlayerSynchronizer(
    castContext: CastContext,
    coroutineScope: CoroutineScope,
    private val castPlayer: PillarboxCastPlayer,
    private val localPlayer: PillarboxExoPlayer,
    private val playerSynchronizer: PlayerSynchronizer = DefaultPlayerSynchronizer(),
) {

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer = castPlayer.isCastSessionAvailableAsFlow()
        .map { if (it) castPlayer else localPlayer }
        .distinctUntilChanged()
        .onEach { switchPlayer(it) }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), if (castContext.isConnected()) castPlayer else localPlayer)

    private fun switchPlayer(player: PillarboxPlayer) {
        val oldPlayer = if (player is PillarboxCastPlayer) localPlayer else castPlayer
        if (oldPlayer == player || (oldPlayer == localPlayer && oldPlayer.playbackState == Player.STATE_IDLE)) return
        val selectedAudioTrack = oldPlayer.currentTracks.audioTracks.firstOrNull { it.isSelected }
        val selectedTextTrack = oldPlayer.currentTracks.textTracks.firstOrNull { it.isSelected }
        player.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                if (!tracks.isEmpty) {
                    val trackSelection =
                        playerSynchronizer.onTracksChanged(
                            newTracks = tracks,
                            selectedAudioTrack = selectedAudioTrack,
                            selectedTextTrack = selectedTextTrack
                        )
                    val newSelectedAudioTrack = trackSelection.audioTrack
                    val newSelectedTextTrack = trackSelection.textTrack
                    newSelectedAudioTrack?.let {
                        player.selectTrack(it)
                    }
                    newSelectedTextTrack?.let {
                        player.selectTrack(it)
                    }
                    player.removeListener(this)
                }
            }
        })

        playerSynchronizer.onPlayerChanged(oldPlayer, player)
    }

    /**
     * Interface to configure the state of two players.
     */
    interface PlayerSynchronizer {

        /**
         * @param oldPlayer the [PillarboxPlayer] that will be replaced.
         * @param newPlayer the [PillarboxPlayer] that will replace the [oldPlayer].
         */
        fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer)

        /**
         * Called when the [Tracks] of the current player are ready to set up the track selection from the previous player.
         *
         * @param newTracks the [Tracks] of the player that replace the previous one.
         * @param selectedAudioTrack the selected [AudioTrack] of previous player, `null` if no audio track is selected.
         * @param selectedTextTrack the selected [TextTrack] of the previous player, `null` if no text track is selected.
         */
        fun onTracksChanged(newTracks: Tracks, selectedAudioTrack: AudioTrack?, selectedTextTrack: TextTrack?): Selection
    }

    /**
     * Default implementation of [PlayerSynchronizer].
     */
    open class DefaultPlayerSynchronizer : PlayerSynchronizer {

        override fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer) {
            newPlayer.repeatMode = oldPlayer.repeatMode
            newPlayer.playWhenReady = oldPlayer.playWhenReady
            newPlayer.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
            newPlayer.prepare()
        }

        override fun onTracksChanged(newTracks: Tracks, selectedAudioTrack: AudioTrack?, selectedTextTrack: TextTrack?): Selection {
            val newSelectedAudioTrack = selectedAudioTrack?.let { track ->
                newTracks.audioTracks.firstOrNull {
                    it.format.language == track.format.language
                }
            }

            val newSelectedTextTrack = selectedTextTrack?.let { track ->
                newTracks.textTracks.firstOrNull {
                    it.format.language == track.format.language
                }
            }
            return Selection(newSelectedAudioTrack, newSelectedTextTrack)
        }
    }

    /**
     * Data class representing the selection of tracks.
     * @property audioTrack The [AudioTrack], `null` if no audio track is selected.
     * @property textTrack The [TextTrack], `null` if no text track is selected.
     */
    class Selection(
        val audioTrack: AudioTrack? = null,
        val textTrack: TextTrack? = null
    )
}
