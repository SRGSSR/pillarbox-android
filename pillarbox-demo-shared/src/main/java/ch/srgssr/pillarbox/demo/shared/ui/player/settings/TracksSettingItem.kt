/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import ch.srgssr.pillarbox.player.tracks.Track

/**
 * The setting for a specific kind a track (audio/text/video).
 */
data class TracksSettingItem(
    /**
     * The title of the setting.
     */
    val title: String,

    /**
     * The list of possible tracks.
     */
    val tracks: List<Track>,

    /**
     * `true` if this kind of tracks is disabled, `false` otherwise.
     */
    val disabled: Boolean
)
