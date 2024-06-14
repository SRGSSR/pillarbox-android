/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

/**
 * Represents the timings until the current media started to play.
 *
 * @property asset The time spent to load the asset.
 * @property drm The time spent to load the DRM.
 * @property mediaSource The time spent to load the media source.
 */
data class QoSSessionTimings(
    val asset: Long,
    val drm: Long,
    val mediaSource: Long,
) {
    /**
     * The total time spent to load all the components.
     */
    val total = asset + drm + mediaSource
}
