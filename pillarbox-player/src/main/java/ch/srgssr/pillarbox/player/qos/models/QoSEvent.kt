/**
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property bandwidth The device-measured network bandwidth, in bits per second.
 * @property bitrate The bitrate of the current stream, in bits per second.
 * @property bufferDuration The forward duration of the buffer, in milliseconds.
 * @property duration The duration of the media being player.
 * @property playbackDuration The duration of the playback, in milliseconds.
 * @property position The position of the player, in milliseconds.
 * @property positionTimestamp The current player timestamp, as retrieved from the playlist.
 * @property stall The information about stalls.
 * @property streamType The type of stream being played.
 * @property url The URL of the stream.
 * @property vpn `true` if a VPN is enabled, `false` otherwise, `null` if the status could not be determined.
 */
data class QoSEvent(
    val bandwidth: Long,
    val bitrate: Long,
    val bufferDuration: Long,
    val duration: Long,
    val playbackDuration: Long,
    val position: Long,
    val positionTimestamp: Long?,
    val stall: QoSStall,
    val streamType: StreamType,
    val url: String,
    val vpn: Boolean?,
) {
    /**
     * The type of stream (live or on demand).
     */
    enum class StreamType {
        LIVE,
        ON_DEMAND,
    }
}
