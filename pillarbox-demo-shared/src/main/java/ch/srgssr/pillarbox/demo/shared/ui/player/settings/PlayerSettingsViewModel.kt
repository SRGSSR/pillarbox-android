/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.currentMetricsAsFlow
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Player settings view model
 *
 * @constructor Create an empty Player settings view model
 */
class PlayerSettingsViewModel(
    private val player: Player,
    private val application: Application,
) : AndroidViewModel(application) {
    private val appSettingsRepository = AppSettingsRepository(application)

    private val trackSelectionParameters = player.getTrackSelectionParametersAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.trackSelectionParameters)

    private val tracks = player.getCurrentTracksAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.currentTracks)

    private val playbackSpeed = player.getPlaybackSpeedAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), player.getPlaybackSpeed())

    private val appSettings = appSettingsRepository.getAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AppSettings())

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
     * All the available video tracks for the current [player].
     */
    val videoTracks = combine(
        tracks,
        trackSelectionParameters,
    ) { tracks, trackSelectionParameters ->
        TracksSettingItem(
            title = application.getString(R.string.video_tracks),
            tracks = tracks.videoTracks
                .sortedWith(
                    compareByDescending<VideoTrack> { it.format.height }
                        .thenByDescending { it.format.bitrate }
                ),
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

    private val currentPlaybackMetrics = if (player is PillarboxExoPlayer) player.currentMetricsAsFlow() else flowOf(null)

    /**
     * All the available settings for the current [player].
     */
    val settings = combine(
        subtitles,
        audioTracks,
        videoTracks,
        trackSelectionParameters,
        playbackSpeed,
        appSettings,
        currentPlaybackMetrics,
    ) { settings ->
        val subtitles = settings[SETTING_INDEX_SUBTITLES] as TracksSettingItem?
        val audioTracks = settings[SETTING_INDEX_AUDIO_TRACKS] as TracksSettingItem?
        val videoTracks = settings[SETTING_INDEX_VIDEO_TRACKS] as TracksSettingItem?
        val trackSelectionParameters = settings[SETTING_INDEX_TRACK_SELECTION_PARAMETERS] as TrackSelectionParameters
        val playbackSpeed = settings[SETTING_INDEX_PLAYBACK_SPEED] as Float
        val appSettings = settings[SETTING_INDEX_APP_SETTINGS] as AppSettings
        val playbackMetrics = settings[SETTING_INDEX_PLAYBACK_METRICS] as PlaybackMetrics?

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

            if (videoTracks != null && videoTracks.tracks.isNotEmpty()) {
                add(
                    SettingItem(
                        title = application.getString(R.string.video_tracks),
                        subtitle = getTracksSubtitle(
                            tracks = videoTracks.tracks,
                            disabled = trackSelectionParameters.isVideoTrackDisabled,
                        ),
                        icon = Icons.Default.Tune,
                        destination = SettingsRoutes.VideoTrack,
                    )
                )
            }

            if (playbackMetrics != null) {
                add(
                    SettingItem(
                        title = application.getString(R.string.metrics_overlay),
                        subtitle = if (appSettings.metricsOverlayEnabled) {
                            application.getString(R.string.metrics_overlay_enabled)
                        } else {
                            application.getString(R.string.metrics_overlay_disabled)
                        },
                        icon = Icons.Default.Analytics,
                        destination = SettingsRoutes.MetricsOverlay(appSettings.metricsOverlayEnabled),
                    )
                )

                add(
                    SettingItem(
                        title = application.getString(R.string.stats_for_nerds),
                        subtitle = null,
                        icon = Icons.Default.Info,
                        destination = SettingsRoutes.StatsForNerds,
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
        player.setAutoTextTrack()
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
        player.setAutoVideoTrack()
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

    /**
     * Enable or disable the metrics overlay.
     *
     * @param enabled
     */
    fun setMetricsOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setMetricsOverlayEnabled(enabled)
        }
    }

    private fun getTracksSubtitle(
        tracks: List<Track>,
        disabled: Boolean,
    ): String? {
        return if (disabled) {
            application.getString(R.string.disabled)
        } else {
            tracks.filter { it.isSelected }
                .map { it.format.displayName }
                .firstOrNull { it != C.LANGUAGE_UNDETERMINED }
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

        private const val SETTING_INDEX_SUBTITLES = 0
        private const val SETTING_INDEX_AUDIO_TRACKS = 1
        private const val SETTING_INDEX_VIDEO_TRACKS = 2
        private const val SETTING_INDEX_TRACK_SELECTION_PARAMETERS = 3
        private const val SETTING_INDEX_PLAYBACK_SPEED = 4
        private const val SETTING_INDEX_APP_SETTINGS = 5
        private const val SETTING_INDEX_PLAYBACK_METRICS = 6
    }

    /**
     * Factory
     *
     * @param player
     * @constructor Create an empty Factory
     */
    class Factory(
        private val player: Player,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])

            @Suppress("UNCHECKED_CAST")
            return PlayerSettingsViewModel(player, application) as T
        }
    }
}
