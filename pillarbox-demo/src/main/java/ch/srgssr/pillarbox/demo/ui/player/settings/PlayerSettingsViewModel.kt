/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeed
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Player settings view model
 *
 * @property player The player to change settings.
 */
class PlayerSettingsViewModel(val player: Player) : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerSettingsUiState>(PlayerSettingsUiState.Home)

    /**
     * Ui state to display the correct settings.
     */
    val uiState = _uiState.asStateFlow()
    private val tracksFlow = player.getCurrentTracksAsFlow()

    /**
     * Text tracks flow
     */
    val textTracksFlow: Flow<List<Tracks.Group>> = tracksFlow.map { tracks ->
        tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
    }

    /**
     * Audio tracks flow
     */
    val audioTracksFlow: Flow<List<Tracks.Group>> = tracksFlow.map { tracks ->
        tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
    }

    /**
     * Current playback rate flow
     */
    val currentPlaybackRateFlow = player.getPlaybackSpeedAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.getPlaybackSpeed())

    /**
     * Open home
     */
    fun openHome() {
        _uiState.value = PlayerSettingsUiState.Home
    }

    /**
     * Open playback rate
     */
    fun openPlaybackRate() {
        _uiState.value = PlayerSettingsUiState.PlaybackRate
    }

    /**
     * Select playback rate
     *
     * @param speed The selected speed
     */
    fun selectPlaybackRate(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    @Suppress("UndocumentedPublicClass")
    class Factory(
        private var player: Player,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerSettingsViewModel(player) as T
        }
    }
}

/**
 * Player settings ui state representing all PlayerSettings screens.
 *
 */
sealed interface PlayerSettingsUiState {
    /**
     * Home where you can choose other screens.
     */
    data object Home : PlayerSettingsUiState

    /**
     * Playback rate
     */
    data object PlaybackRate : PlayerSettingsUiState

    /**
     * Text tracks
     */
    data object TextTracks : PlayerSettingsUiState
}
