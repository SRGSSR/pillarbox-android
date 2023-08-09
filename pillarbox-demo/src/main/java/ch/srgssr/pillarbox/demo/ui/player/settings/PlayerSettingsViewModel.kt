/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.extension.audio
import ch.srgssr.pillarbox.player.extension.text
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeed
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.getTrackSelectionParametersAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Player settings view model
 *
 * @property player
 * @constructor Create empty Player settings view model
 */
class PlayerSettingsViewModel(val player: Player) : ViewModel() {
    private val _tracks = player.getCurrentTracksAsFlow()

    /**
     * Track selection parameters
     */
    val trackSelectionParameters =
        player.getTrackSelectionParametersAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.trackSelectionParameters)

    /**
     * Text tracks
     */
    val textTracks = _tracks.map { it.text }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.currentTracks.text)

    /**
     * Audio tracks
     */
    val audioTracks = _tracks.map { it.audio }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.currentTracks.audio)

    /**
     * Has subtitles
     */
    val hasSubtitles = textTracks.map {
        it.isNotEmpty()
    }

    /**
     * Has audio
     */
    val hasAudio = audioTracks.map {
        it.isNotEmpty()
    }

    /**
     * Playback speed
     */
    val playbackSpeed = player.getPlaybackSpeedAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.getPlaybackSpeed())

    /**
     * Factory
     *
     * @property player
     * @constructor Create empty Factory
     */
    @Suppress("UndocumentedPublicClass")
    class Factory(
        private var player: Player,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerSettingsViewModel(player) as T
        }
    }
}
