/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks.Group
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.player.extension.audio
import ch.srgssr.pillarbox.player.extension.disableAudioTrack
import ch.srgssr.pillarbox.player.extension.disableTextTrack
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.extension.isAudioTrackDisabled
import ch.srgssr.pillarbox.player.extension.isTextTrackDisabled
import ch.srgssr.pillarbox.player.extension.setDefaultAudioTrack
import ch.srgssr.pillarbox.player.extension.setDefaultTextTrack
import ch.srgssr.pillarbox.player.extension.setTrackOverride
import ch.srgssr.pillarbox.player.extension.text
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.getTrackSelectionParametersAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Player settings view model
 *
 * @constructor Create empty Player settings view model
 */
class PlayerSettingsViewModel(
    private val player: Player,
    private val application: Application
) : AndroidViewModel(application) {
    private val trackSelectionParameters = player.getTrackSelectionParametersAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.trackSelectionParameters)

    private val tracks = player.getCurrentTracksAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.currentTracks)

    private val playbackSpeed = player.getPlaybackSpeedAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.getPlaybackSpeed())

    /**
     * All the available settings for the current [player].
     */
    val settings = combine(
        tracks,
        trackSelectionParameters,
        playbackSpeed
    ) { currentTracks, trackSelectionParameters, playbackSpeed ->
        buildList {
            add(
                SettingItem(
                    title = application.getString(R.string.speed),
                    subtitle = getSpeedLabel(playbackSpeed),
                    icon = Icons.Default.Speed,
                    destination = SettingsRoutes.PlaybackSpeed
                )
            )

            if (currentTracks.text.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.subtitles),
                        subtitle = getTracksSubtitle(
                            tracks = currentTracks.text,
                            disabled = trackSelectionParameters.isTextTrackDisabled
                        ),
                        icon = Icons.Default.Subtitles,
                        destination = SettingsRoutes.Subtitles
                    )
                )
            }

            if (currentTracks.audio.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.audio_track),
                        subtitle = getTracksSubtitle(
                            tracks = currentTracks.audio,
                            disabled = trackSelectionParameters.isAudioTrackDisabled
                        ),
                        icon = Icons.Default.Audiotrack,
                        destination = SettingsRoutes.AudioTrack
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     * All the available subtitle for the current [player].
     */
    val subtitles = combine(
        tracks,
        trackSelectionParameters
    ) { tracks, trackSelectionParameters ->
        TracksSettingItem(
            title = application.getString(R.string.subtitles),
            tracks = tracks.text,
            disabled = trackSelectionParameters.isTextTrackDisabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * All the available audio tracks for the current [player].
     */
    val audioTracks = combine(
        tracks,
        trackSelectionParameters
    ) { tracks, trackSelectionParameters ->
        TracksSettingItem(
            title = application.getString(R.string.audio_track),
            tracks = tracks.audio,
            disabled = trackSelectionParameters.isAudioTrackDisabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * All the available playback speeds for the current [player].
     */
    val playbackSpeeds = playbackSpeed.map { playbackSpeed ->
        speeds.map { speed ->
            PlaybackSpeedSetting(
                speed = getSpeedLabel(speed),
                rawSpeed = speed,
                isSelected = speed == playbackSpeed
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     * Reset the subtitles.
     */
    fun resetSubtitles() {
        player.setDefaultTextTrack(application)
    }

    /**
     * Disable the subtitles.
     */
    fun disableSubtitles() {
        player.disableTextTrack()
    }

    /**
     * Set the subtitles.
     *
     * @param group The selected group.
     * @param trackIndex The index of the track in the provided group.
     */
    fun setSubtitle(group: Group, trackIndex: Int) {
        player.setTrackOverride(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
    }

    /**
     * Reset the audio track.
     */
    fun resetAudioTrack() {
        player.setDefaultAudioTrack(application)
    }

    /**
     * Disable the audio track.
     */
    fun disableAudioTrack() {
        player.disableAudioTrack()
    }

    /**
     * Set the audio track.
     *
     * @param group The selected group.
     * @param trackIndex The index of the track in the provided group.
     */
    fun setAudioTrack(group: Group, trackIndex: Int) {
        player.setTrackOverride(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
    }

    /**
     * Set the playback speed.
     *
     * @param playbackSpeed The selected playback speed.
     */
    fun setPlaybackSpeed(playbackSpeed: PlaybackSpeedSetting) {
        player.setPlaybackSpeed(playbackSpeed.rawSpeed)
    }

    private fun getTracksSubtitle(
        tracks: List<Group>,
        disabled: Boolean
    ): String? {
        return if (disabled) {
            application.getString(R.string.disabled)
        } else {
            tracks.filter { it.isSelected }
                .flatMap {
                    (0 until it.length).mapNotNull { trackIndex ->
                        if (it.isTrackSelected(trackIndex)) {
                            it.getTrackFormat(trackIndex).displayName
                        } else {
                            null
                        }
                    }
                }
                .firstOrNull()
        }
    }

    private fun getSpeedLabel(speed: Float): String {
        return if (speed == 1f) {
            application.getString(R.string.speed_normal)
        } else {
            application.getString(R.string.speed_value, speed.toString())
        }
    }

    private companion object {
        private val speeds = floatArrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    }

    /**
     * Factory
     *
     * @param player
     * @param application
     * @constructor Create empty Factory
     */
    @Suppress("UndocumentedPublicClass")
    class Factory(
        private val player: Player,
        private val application: Application
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerSettingsViewModel(player, application) as T
        }
    }
}
