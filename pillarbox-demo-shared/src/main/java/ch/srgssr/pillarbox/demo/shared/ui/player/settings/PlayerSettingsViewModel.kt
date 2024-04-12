/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.extension.isAudioTrackDisabled
import ch.srgssr.pillarbox.player.extension.isTextTrackDisabled
import ch.srgssr.pillarbox.player.extension.isVideoTrackDisabled
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.getTrackSelectionParametersAsFlow
import ch.srgssr.pillarbox.player.tracks.Track
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.tracks.disableAudioTrack
import ch.srgssr.pillarbox.player.tracks.disableTextTrack
import ch.srgssr.pillarbox.player.tracks.disableVideoTrack
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.setAutoAudioTrack
import ch.srgssr.pillarbox.player.tracks.setAutoTextTrack
import ch.srgssr.pillarbox.player.tracks.setAutoVideoTrack
import ch.srgssr.pillarbox.player.tracks.textTracks
import ch.srgssr.pillarbox.player.tracks.videoTracks
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
     * All the available subtitle for the current [player].
     */
    val subtitles = combine(
        tracks,
        trackSelectionParameters
    ) { tracks, trackSelectionParameters ->
        TracksSettingItem(
            title = application.getString(R.string.subtitles),
            tracks = tracks.textTracks,
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
            tracks = tracks.audioTracks,
            disabled = trackSelectionParameters.isAudioTrackDisabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * All the available video qualities for the current [player].
     */
    val videoQualities = combine(
        tracks,
        trackSelectionParameters,
    ) { tracks, trackSelectionParameters ->
        TracksSettingItem(
            title = application.getString(R.string.quality),
            tracks = tracks.videoTracks
                .distinctBy { it.format.height }
                .sortedByDescending { it.format.height },
            disabled = trackSelectionParameters.isVideoTrackDisabled,
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
     * All the available settings for the current [player].
     */
    val settings = combine(
        subtitles,
        audioTracks,
        videoQualities,
        trackSelectionParameters,
        playbackSpeed,
    ) { subtitles, audioTracks, videoQualities, trackSelectionParameters, playbackSpeed ->
        buildList {
            add(
                SettingItem(
                    title = application.getString(R.string.speed),
                    subtitle = getSpeedLabel(playbackSpeed),
                    icon = Icons.Default.SlowMotionVideo,
                    destination = SettingsRoutes.PlaybackSpeed,
                )
            )

            if (subtitles != null && subtitles.tracks.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.subtitles),
                        subtitle = getTracksSubtitle(
                            tracks = subtitles.tracks,
                            disabled = trackSelectionParameters.isTextTrackDisabled,
                        ),
                        icon = Icons.Default.ClosedCaption,
                        destination = SettingsRoutes.Subtitles,
                    )
                )
            }

            if (audioTracks != null && audioTracks.tracks.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.audio_track),
                        subtitle = getTracksSubtitle(
                            tracks = audioTracks.tracks,
                            disabled = trackSelectionParameters.isAudioTrackDisabled,
                        ),
                        icon = Icons.Default.RecordVoiceOver,
                        destination = SettingsRoutes.AudioTrack,
                    )
                )
            }

            if (videoQualities != null && videoQualities.tracks.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.quality),
                        subtitle = getTracksSubtitle(
                            tracks = videoQualities.tracks,
                            disabled = trackSelectionParameters.isVideoTrackDisabled,
                        ),
                        icon = Icons.Default.Tune,
                        destination = SettingsRoutes.VideoQuality,
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     * Select a specific track.
     *
     * @param track The track to select.
     */
    fun selectTrack(track: Track) {
        player.selectTrack(track)
    }

    /**
     * Reset the subtitles.
     */
    fun resetSubtitles() {
        player.setAutoTextTrack(application)
    }

    /**
     * Disable the subtitles.
     */
    fun disableSubtitles() {
        player.disableTextTrack()
    }

    /**
     * Reset the audio track.
     */
    fun resetAudioTrack() {
        player.setAutoAudioTrack(application)
    }

    /**
     * Disable the audio track.
     */
    fun disableAudioTrack() {
        player.disableAudioTrack()
    }

    /**
     * Reset the video track.
     */
    fun resetVideoTrack() {
        player.setAutoVideoTrack(application)
    }

    /**
     * Disable the video track.
     */
    fun disableVideoTrack() {
        player.disableVideoTrack()
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
        tracks: List<Track>,
        disabled: Boolean
    ): String? {
        return if (disabled) {
            application.getString(R.string.disabled)
        } else {
            tracks.filter { it.isSelected }
                .map {
                    if (it is VideoTrack) {
                        it.format.height.toString() + "p"
                    } else {
                        it.format.displayName
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
