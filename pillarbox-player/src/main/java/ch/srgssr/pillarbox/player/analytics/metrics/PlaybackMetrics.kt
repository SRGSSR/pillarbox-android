/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import kotlin.time.Duration

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property sessionId The session ID.
 * @property bandwidth The device-measured network bandwidth, in bytes per second.
 * @property bitrate The bitrate of the current stream, in bytes per second.
 * @property bufferDuration The forward duration of the buffer.
 * @property playbackDuration The duration of the playback.
 * @property stallCount The number of stalls that have occurred, not as a result of a seek.
 * @property stallDuration The total duration of the stalls.
 * @property loadDuration The load duration that could be computed.
 */
data class PlaybackMetrics(
    val sessionId: String,
    val bandwidth: Long = 0,
    val bitrate: Int = 0,
    val bufferDuration: Duration = Duration.ZERO,
    val playbackDuration: Duration = Duration.ZERO,
    val stallCount: Int = 0,
    val stallDuration: Duration = Duration.ZERO,
    val loadDuration: LoadDuration = LoadDuration()
) {

    /**
     * Load duration
     * Represents the timings until the current media started to play.
     * @property source The time spent to load the media source.
     * @property manifest The time spent to load the main manifest if applicable.
     * @property asset The time spent to load the asset.
     * @property drm The time spent to load the DRM.
     * @property timeToReady The time spent to load from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until
     * it started to play.
     */
    data class LoadDuration(
        val source: Duration? = null,
        val manifest: Duration? = null,
        val asset: Duration? = null,
        val drm: Duration? = null,
        val timeToReady: Duration? = null
    )
}
