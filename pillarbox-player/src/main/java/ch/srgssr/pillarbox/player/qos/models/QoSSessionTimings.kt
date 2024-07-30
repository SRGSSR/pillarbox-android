/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlin.time.Duration

/**
 * Represents the timings until the current media started to play.
 *
 * @property asset The time spent to load the asset.
 * @property drm The time spent to load the DRM.
 * @property metadata The time spent to load the media source.
 * @property total The time spent to load from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until it
 * started to play.
 */
data class QoSSessionTimings(
    val asset: Duration? = null,
    val drm: Duration? = null,
    val metadata: Duration? = null,
    val total: Duration? = null,
) {
    companion object {
        /**
         * Default [QoSSessionTimings] where all fields are set to `null`.
         */
        val Empty = QoSSessionTimings(
            asset = null,
            drm = null,
            metadata = null,
            total = null,
        )
    }
}
