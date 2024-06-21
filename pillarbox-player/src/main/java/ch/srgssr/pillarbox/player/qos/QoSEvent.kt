/**
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property bandwidth The device-measured network bandwidth, in bytes per second.
 * @property bitrate The bitrate of the current stream, in bytes per second.
 * @property bufferDuration The forward duration of the buffer, in milliseconds.
 * @property playbackDuration The duration of the playback, in milliseconds.
 * @property playerPosition The position of the player, in milliseconds.
 * @property stallCount The number of stalls that have occurred, not as a result of a seek.
 * @property stallDuration The total duration of the stalls, in milliseconds.
 * @property url The URL of the stream.
 */
data class QoSEvent(
    val bandwidth: Long,
    val bitrate: Long,
    val bufferDuration: Long,
    val playbackDuration: Long,
    val playerPosition: Long,
    val stallCount: Long,
    val stallDuration: Long,
    val url: String,
)
