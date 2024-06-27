/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import kotlin.time.Duration

/**
 * Represents the timings until the current media started to play.
 *
 * @property asset The time spent to load the asset.
 * @property currentToStart The time spent to load from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until it
 * started to play.
 * @property drm The time spent to load the DRM.
 * @property mediaSource The time spent to load the media source.
 */
data class QoSSessionTimings(
    val asset: Duration,
    val currentToStart: Duration,
    val drm: Duration,
    val mediaSource: Duration,
) {
    companion object {
        /**
         * Default [QoSSessionTimings] where all fields are a duration of zero.
         */
        val Zero = QoSSessionTimings(
            asset = Duration.ZERO,
            currentToStart = Duration.ZERO,
            drm = Duration.ZERO,
            mediaSource = Duration.ZERO,
        )
    }
}
