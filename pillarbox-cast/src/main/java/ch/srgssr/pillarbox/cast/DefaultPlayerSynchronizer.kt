/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.util.Log
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.tracks.textTracks

/**
 * Default implementation of [PlayerSynchronizer].
 */
open class DefaultPlayerSynchronizer : PlayerSynchronizer {

    override fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer) {
        Log.d("Coucou", "onPlayerChanged $oldPlayer -> $newPlayer")
        Log.d("Coucou", "items = ${oldPlayer.getCurrentMediaItems().map { it.mediaMetadata.title }}")
        newPlayer.repeatMode = oldPlayer.repeatMode
        newPlayer.playWhenReady = oldPlayer.playWhenReady
        newPlayer.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
    }

    override fun onTracksChanged(
        newTracks: Tracks,
        selectedAudioTrack: AudioTrack?,
        selectedTextTrack: TextTrack?
    ): PlayerSynchronizer.Selection {
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
        return PlayerSynchronizer.Selection(newSelectedAudioTrack, newSelectedTextTrack)
    }
}
