/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import kotlinx.serialization.Serializable

/**
 * All the routes used in the player's settings.
 */
@Serializable
sealed interface SettingsRoutes {
    /**
     * The route for the main screen of the settings.
     */
    @Serializable
    data object Main : SettingsRoutes

    /**
     * The route for the playback speed setting.
     */
    @Serializable
    data object PlaybackSpeed : SettingsRoutes

    /**
     * The route for the subtitles setting.
     */
    @Serializable
    data object Subtitles : SettingsRoutes

    /**
     * The route for the audio track setting.
     */
    @Serializable
    data object AudioTrack : SettingsRoutes

    /**
     * The route for the video track setting.
     */
    @Serializable
    data object VideoTrack : SettingsRoutes

    /**
     * The route for the metrics overlay setting.
     *
     * @property enabled Whether the metrics overlay is enabled.
     */
    @Serializable
    data class MetricsOverlay(
        val enabled: Boolean,
    ) : SettingsRoutes

    /**
     * The route for the "Stats for nerds" screen.
     */
    @Serializable
    data object StatsForNerds : SettingsRoutes
}
