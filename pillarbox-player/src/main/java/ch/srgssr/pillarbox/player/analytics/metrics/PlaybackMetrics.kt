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
 * Represents playback metrics, containing information about the current media stream and playback session.
 *
 * @property sessionId A unique identifier for the playback session.
 * @property bandwidth The device-measured network bandwidth, in bits per second.
 * @property indicatedBitrate The bitrate of the video and audio format, in bits per second.
 * @property playbackDuration The total duration spent playing the media.
 * @property bufferingDuration The total duration spent buffering the media.
 * @property stallCount The number of times playback stalled, excluding stalls caused by seeking.
 * @property stallDuration The total duration of the stalls.
 * @property loadDuration The [LoadDuration] containing detailed timings for different stages of media loading.
 * @property totalLoadTime The total time taken to load the media for bandwidth calculation.
 * @property totalBytesLoaded The total number of bytes loaded for bandwidth calculation.
 * @property url The last URL loaded by the player.
 * @property videoFormat The [Format] of the currently selected video track.
 * @property audioFormat The [Format] of the currently selected audio track.
 * @property surfaceSize The size of the surface used for rendering the video. If no surface is connected, this will be [Size.ZERO].
 * @property totalDroppedFrames The total number of video frames dropped.
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
    val totalDroppedFrames: Int,
) {

    /**
     * Represents the video size of [videoFormat], if applicable. Otherwise [VideoSize.UNKNOWN].
     */
    val videoSize: VideoSize = videoFormat?.videoSize ?: VideoSize.UNKNOWN

    /**
     * Represents the timings spent in different stages of the loading process, until the current media started to play.
     *
     * @property source The time spent loading the media source.
     * @property manifest The time spent loading the main manifest, if applicable.
     * @property asset The time spent loading the asset.
     * @property drm The time spent loading the DRM.
     * @property timeToReady The total time elapsed from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until it
     * was ready to play.
     */
    data class LoadDuration(
        val source: Duration? = null,
        val manifest: Duration? = null,
        val asset: Duration? = null,
        val drm: Duration? = null,
        val timeToReady: Duration? = null
    )
}
