/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a generic event, which contains metrics about the current media stream.
 *
 * @property bandwidth The device-measured network bandwidth, in bits per second.
 * @property bitrate The bitrate of the current stream, in bits per second.
 * @property bufferDuration The forward duration of the buffer, in milliseconds.
 * @property duration The duration of the media being player, in milliseconds.
 * @property playbackDuration The duration of the playback, in milliseconds.
 * @property position The position of the player, in milliseconds.
 * @property positionTimestamp The current player timestamp, as retrieved from the playlist.
 * @property stall The information about stalls.
 * @property streamType The type of stream being played.
 * @property url The URL of the stream.
 * @property vpn `true` if a VPN is enabled, `false` otherwise, `null` if the status could not be determined.
 */
@Serializable
data class EventMessageData(
    val bandwidth: Long,
    val bitrate: Long,
    @SerialName("buffered_duration") val bufferDuration: Long,
    val duration: Long,
    @SerialName("playback_duration") val playbackDuration: Long,
    val position: Long,
    @SerialName("position_timestamp") val positionTimestamp: Long?,
    val stall: Stall,
    @SerialName("stream_type") val streamType: StreamType,
    val url: String,
    val vpn: Boolean?,
) : MessageData {
    /**
     * The type of stream (live or on demand).
     */
    @Suppress("UndocumentedPublicProperty")
    enum class StreamType {
        @SerialName("Live")
        LIVE,

        @SerialName("On-demand")
        ON_DEMAND,
    }

    /**
     * Information about stalls.
     *
     * @property count The number of stalls that have occurred, not as a result of a seek.
     * @property duration The total duration of the stalls, in milliseconds.
     */
    @Serializable
    data class Stall(
        val count: Int,
        val duration: Long,
    )
}
