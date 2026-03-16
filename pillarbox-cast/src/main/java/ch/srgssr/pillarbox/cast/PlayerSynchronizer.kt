/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack

/**
 * Interface to configure the state of two players.
 */
interface PlayerSynchronizer {

    /**
     * @param oldPlayer the [ch.srgssr.pillarbox.player.PillarboxPlayer] that will be replaced.
     * @param newPlayer the [ch.srgssr.pillarbox.player.PillarboxPlayer] that will replace the [oldPlayer].
     */
    fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer)

    /**
     * Called when the [androidx.media3.common.Tracks] of the current player are ready to set up the track selection from the previous player.
     *
     * @param newTracks the [androidx.media3.common.Tracks] of the player that replace the previous one.
     * @param selectedAudioTrack the selected [ch.srgssr.pillarbox.player.tracks.AudioTrack] of previous player, `null` if no audio track is selected.
     * @param selectedTextTrack the selected [ch.srgssr.pillarbox.player.tracks.TextTrack] of the previous player, `null` if no text track is selected.
     */
    fun onTracksChanged(newTracks: Tracks, selectedAudioTrack: AudioTrack?, selectedTextTrack: TextTrack?): Selection

    class Selection(
        val audioTrack: AudioTrack? = null,
        val textTrack: TextTrack? = null
    )
}
