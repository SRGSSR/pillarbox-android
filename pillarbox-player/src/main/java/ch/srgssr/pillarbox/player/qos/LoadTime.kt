/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import kotlin.time.Duration

/**
 * Represents the load time of a media item, divided into several components.
 *
 * @property ad The time spent loading ads.
 * @property custom The time spent loading custom data.
 * @property drm The time spent loading the DRM license.
 * @property manifest The time spent loading manifests.
 * @property media The time spent loading the media.
 * @property mediaInitialization The time spent initializing the media.
 * @property mediaProgressiveLive The time spent loading media in a progressive live stream.
 * @property timeSynchronization The time spent loading time synchronization data.
 * @property unknown The time spent on unknown load operations.
 */
data class LoadTime(
    val ad: Duration = Duration.ZERO,
    val custom: Map<Int, Duration> = emptyMap(),
    val drm: Duration = Duration.ZERO,
    val manifest: Duration = Duration.ZERO,
    val media: Duration = Duration.ZERO,
    val mediaInitialization: Duration = Duration.ZERO,
    val mediaProgressiveLive: Duration = Duration.ZERO,
    val timeSynchronization: Duration = Duration.ZERO,
    val unknown: Duration = Duration.ZERO,
) {
    /**
     * The total load time of the media item.
     */
    val totalLoadTime: Duration
        get() = ad + totalCustomLoadTime + drm + manifest + media + mediaInitialization + mediaProgressiveLive + timeSynchronization + unknown

    /**
     * The total load time for custom data of the media item.
     */
    val totalCustomLoadTime: Duration
        get() = custom.values.fold(Duration.ZERO) { result, value -> result + value }

    companion object {
        /**
         * An empty instance of `LoadTime`.
         */
        val Empty = LoadTime()
    }
}
