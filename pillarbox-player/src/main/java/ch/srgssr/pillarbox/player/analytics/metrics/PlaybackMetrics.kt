/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import android.net.Uri
import androidx.media3.common.Format
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Size
import ch.srgssr.pillarbox.player.extension.videoSize
import kotlin.time.Duration

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property sessionId The session ID.
 * @property bandwidth The device-measured network bandwidth, in bytes per second.
 * @property indicatedBitrate The bitrate of the video and audio format.
 * @property playbackDuration The duration the session spent playing.
 * @property bufferingDuration The duration the session spent in buffering.
 * @property stallCount The number of stalls that have occurred, not as a result of a seek.
 * @property stallDuration The total duration of the stalls.
 * @property loadDuration The load duration that could be computed.
 * @property totalLoadTime The load time to compute [bandwidth].
 * @property totalBytesLoaded The total bytes loaded to compute [bandwidth].
 * @property url The last url loaded by the player.
 * @property videoFormat The current video format selected by the player.
 * @property audioFormat The current audio format selected by the player.
 * @property surfaceSize The size of the surface connected to the player. [Size.ZERO] if not connected.
 */
data class PlaybackMetrics(
    val sessionId: String,
    val bandwidth: Long,
    val indicatedBitrate: Long,
    val playbackDuration: Duration,
    val bufferingDuration: Duration,
    val stallCount: Int,
    val stallDuration: Duration,
    val loadDuration: LoadDuration,
    val totalLoadTime: Duration,
    val totalBytesLoaded: Long,
    val url: Uri?,
    val videoFormat: Format?,
    val audioFormat: Format?,
    val surfaceSize: Size,
) {

    /**
     * Video size of [videoFormat] if applicable.
     */
    val videoSize: VideoSize = videoFormat?.videoSize ?: VideoSize.UNKNOWN

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
