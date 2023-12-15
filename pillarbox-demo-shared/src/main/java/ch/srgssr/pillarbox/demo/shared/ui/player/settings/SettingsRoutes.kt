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
}
