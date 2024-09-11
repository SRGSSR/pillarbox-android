/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import kotlinx.serialization.Serializable

/**
 * Timings
 */
object Timings {
    /**
     * Represents the timings until the current media started to play, as experienced by the user.
     *
     * @property asset The time spent to load the asset, in milliseconds.
     * @property metadata The time spent to load the media source, in milliseconds.
     * @property total The time spent to load from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until it
     * started to play, in milliseconds.
     */
    @Serializable
    data class QoE(
        val asset: Long? = null,
        val metadata: Long? = null,
        val total: Long? = null,
    )

    /**
     * Represents the timings until the current media started to play, during the preload phase.
     *
     * @property asset The time spent to load the asset, in milliseconds.
     * @property drm The time spent to load the DRM, in milliseconds.
     * @property metadata The time spent to load the media source, in milliseconds.
     * @property token The time spent to load the token, in milliseconds.
     */
    @Serializable
    data class QoS(
        val asset: Long? = null,
        val drm: Long? = null,
        val metadata: Long? = null,
        val token: Long? = null,
    )
}
