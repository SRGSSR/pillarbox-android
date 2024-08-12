/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

/**
 * All the routes used in the player's settings.
 *
 * @property route The route of the setting.
 */
sealed class SettingsRoutes(val route: String) {
    /**
     * The route for the main screen of the settings.
     */
    data object Main : SettingsRoutes(route = "settings")

    /**
     * The route for the playback speed setting.
     */
    data object PlaybackSpeed : SettingsRoutes(route = "settings/playback_speed")

    /**
     * The route for the subtitles setting.
     */
    data object Subtitles : SettingsRoutes(route = "settings/subtitles")

    /**
     * The route for the audio track setting.
     */
    data object AudioTrack : SettingsRoutes(route = "settings/audio_track")

    /**
     * The route for the video track setting.
     */
    data object VideoTrack : SettingsRoutes(route = "settings/video_track")

    /**
     * The route for the metrics overlay setting.
     *
     * @property enabled Whether the metrics overlay is enabled.
     */
    data class MetricsOverlay(
        val enabled: Boolean,
    ) : SettingsRoutes(route = "settings/metrics_overlay")

    /**
     * The route for the "Stats for nerds" screen.
     */
    data object StatsForNerds : SettingsRoutes(route = "settings/stats_for_nerds")
}
