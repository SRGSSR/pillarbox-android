/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.settings

/**
 * A possible playback speed value.
 */
data class PlaybackSpeedSetting(
    /**
     * The formatted speed.
     */
    val speed: String,

    /**
     * The speed as [Float].
     */
    val rawSpeed: Float,

    /**
     * `true` if this speed is selected, `false` otherwise.
     */
    val isSelected: Boolean
)
