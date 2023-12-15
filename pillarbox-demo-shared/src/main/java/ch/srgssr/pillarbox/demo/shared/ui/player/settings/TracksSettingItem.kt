/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

import androidx.media3.common.Tracks.Group

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
    val tracks: List<Group>,

    /**
     * `true` if this kind of tracks is disabled, `false` otherwise.
     */
    val disabled: Boolean
) {
    companion object {
        /**
         * Default tracks setting, which has no title, and no values.
         */
        val empty = TracksSettingItem(
            title = "",
            tracks = emptyList(),
            disabled = true
        )
    }
}
