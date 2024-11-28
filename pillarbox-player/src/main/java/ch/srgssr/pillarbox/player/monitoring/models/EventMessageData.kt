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
 * @property duration The duration of the media being played, in milliseconds.
 * @property playbackDuration The duration of the playback, in milliseconds.
 * @property position The current playback position of the player, in milliseconds.
 * @property positionTimestamp The current player timestamp, as retrieved from the playlist.
 * @property stall Information about stalls that have occurred during playback.
 * @property streamType The type of stream being played.
 * @property url The URL of the stream being played.
 * @property vpn Indicates whether a VPN is enabled, or if the status could not be determined.
 * @property frameDrops The number of frame drops that have occurred during playback.
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
    @SerialName("frame_drops")
    val frameDrops: Int,
) : MessageData {
    /**
     * Represents the type of a media stream.
     */
    @Suppress("UndocumentedPublicProperty")
    enum class StreamType {
        @SerialName("Live")
        LIVE,

        @SerialName("On-demand")
        ON_DEMAND,
    }

    /**
     * Represents information about stalls that occur during playback.
     *
     * @property count The total number of stalls that have occurred, excluding stalls caused by explicit seeks.
     * @property duration The accumulated duration of all stalls, in milliseconds.
     */
    @Serializable
    data class Stall(
        val count: Int,
        val duration: Long,
    )
}
