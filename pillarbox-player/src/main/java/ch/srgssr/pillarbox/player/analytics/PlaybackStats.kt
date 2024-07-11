/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import kotlin.time.Duration

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property bandwidth The device-measured network bandwidth, in bytes per second.
 * @property bitrate The bitrate of the current stream, in bytes per second.
 * @property bufferDuration The forward duration of the buffer, in milliseconds.
 * @property playbackDuration The duration of the playback, in milliseconds.
 * @property stallCount The number of stalls that have occurred, not as a result of a seek.
 * @property stallDuration The total duration of the stalls, in milliseconds.
 */
data class PlaybackStats(
    val bandwidth: Long,
    val bitrate: Int,
    val bufferDuration: Duration,
    val playbackDuration: Duration,
    val stallCount: Int,
    val stallDuration: Duration,
)
