/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.State
import androidx.media3.common.Player.TimelineChangeReason

/**
 * A utility class that provides string representations for various [Player] constants and enums.
 */
object StringUtil {
    private const val UNKNOWN = "UNKNOWN"

    /**
     * Converts a media item transition reason integer value to its corresponding string representation.
     *
     * @param value The [MediaItemTransitionReason].
     * @return A string representation of the media item transition reason.
     */
    fun mediaItemTransitionReasonString(value: @MediaItemTransitionReason Int): String {
        return when (value) {
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "MEDIA_ITEM_TRANSITION_REASON_AUTO"
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> "MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED"
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "MEDIA_ITEM_TRANSITION_REASON_SEEK"
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "MEDIA_ITEM_TRANSITION_REASON_REPEAT"
            else -> UNKNOWN
        }
    }

    /**
     * Converts a player state integer value to its corresponding string representation.
     *
     * @param value The [State].
     * @return A string representation of the player state.
     */
    fun playerStateString(value: @State Int): String {
        return when (value) {
            Player.STATE_ENDED -> "STATE_ENDED"
            Player.STATE_READY -> "STATE_READY"
            Player.STATE_BUFFERING -> "STATE_BUFFERING"
            Player.STATE_IDLE -> "STATE_IDLE"
            else -> UNKNOWN
        }
    }

    /**
     * Converts a timeline change reason integer value to its corresponding string representation.
     *
     * @param value The [TimelineChangeReason].
     * @return A string representation of the timeline change reason.
     */
    fun timelineChangeReasonString(value: @TimelineChangeReason Int): String {
        return when (value) {
            Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED"
            Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "TIMELINE_CHANGE_REASON_SOURCE_UPDATE"
            else -> UNKNOWN
        }
    }

    /**
     * Converts a discontinuity reason integer value to its corresponding string representation.
     *
     * @param value The [DiscontinuityReason].
     * @return A string representation of the discontinuity reason.
     */
    fun discontinuityReasonString(value: @DiscontinuityReason Int): String {
        return when (value) {
            Player.DISCONTINUITY_REASON_SEEK -> "DISCONTINUITY_REASON_SEEK"
            Player.DISCONTINUITY_REASON_REMOVE -> "DISCONTINUITY_REASON_REMOVE"
            Player.DISCONTINUITY_REASON_INTERNAL -> "DISCONTINUITY_REASON_INTERNAL"
            Player.DISCONTINUITY_REASON_SKIP -> "DISCONTINUITY_REASON_SKIP"
            Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "DISCONTINUITY_REASON_SEEK_ADJUSTMENT"
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "DISCONTINUITY_REASON_AUTO_TRANSITION"
            Player.DISCONTINUITY_REASON_SILENCE_SKIP -> "DISCONTINUITY_REASON_SILENCE_SKIP"
            else -> UNKNOWN
        }
    }

    /**
     * Converts player commands to a human readable string.
     *
     * @param value The [Player.Commands].
     *
     * @return A string representation of the player commands.
     */
    @Suppress("CyclomaticComplexMethod")
    fun playerCommandsString(value: Player.Commands): String {
        return buildList {
            repeat(value.size()) {
                val commandName = when (val command = value.get(it)) {
                    Player.COMMAND_INVALID -> "COMMAND_INVALID"
                    Player.COMMAND_PLAY_PAUSE -> "COMMAND_PLAY_PAUSE"
                    Player.COMMAND_PREPARE -> "COMMAND_PREPARE"
                    Player.COMMAND_STOP -> "COMMAND_STOP"
                    Player.COMMAND_SEEK_TO_DEFAULT_POSITION -> "COMMAND_SEEK_TO_DEFAULT_POSITION"
                    Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM -> "COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM"
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> "COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM"
                    Player.COMMAND_SEEK_TO_PREVIOUS -> "COMMAND_SEEK_TO_PREVIOUS"
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> "COMMAND_SEEK_TO_NEXT_MEDIA_ITEM"
                    Player.COMMAND_SEEK_TO_NEXT -> "COMMAND_SEEK_TO_NEXT"
                    Player.COMMAND_SEEK_TO_MEDIA_ITEM -> "COMMAND_SEEK_TO_MEDIA_ITEM"
                    Player.COMMAND_SEEK_BACK -> "COMMAND_SEEK_BACK"
                    Player.COMMAND_SEEK_FORWARD -> "COMMAND_SEEK_FORWARD"
                    Player.COMMAND_SET_SPEED_AND_PITCH -> "COMMAND_SET_SPEED_AND_PITCH"
                    Player.COMMAND_SET_SHUFFLE_MODE -> "COMMAND_SET_SHUFFLE_MODE"
                    Player.COMMAND_SET_REPEAT_MODE -> "COMMAND_SET_REPEAT_MODE"
                    Player.COMMAND_GET_CURRENT_MEDIA_ITEM -> "COMMAND_GET_CURRENT_MEDIA_ITEM"
                    Player.COMMAND_GET_TIMELINE -> "COMMAND_GET_TIMELINE"
                    Player.COMMAND_GET_METADATA -> "COMMAND_GET_METADATA"
                    Player.COMMAND_SET_PLAYLIST_METADATA -> "COMMAND_SET_PLAYLIST_METADATA"
                    Player.COMMAND_SET_MEDIA_ITEM -> "COMMAND_SET_MEDIA_ITEM"
                    Player.COMMAND_CHANGE_MEDIA_ITEMS -> "COMMAND_CHANGE_MEDIA_ITEMS"
                    Player.COMMAND_GET_AUDIO_ATTRIBUTES -> "COMMAND_GET_AUDIO_ATTRIBUTES"
                    Player.COMMAND_GET_VOLUME -> "COMMAND_GET_VOLUME"
                    Player.COMMAND_GET_DEVICE_VOLUME -> "COMMAND_GET_DEVICE_VOLUME"
                    Player.COMMAND_SET_VOLUME -> "COMMAND_SET_VOLUME"
                    Player.COMMAND_SET_DEVICE_VOLUME -> "COMMAND_SET_DEVICE_VOLUME"
                    Player.COMMAND_SET_DEVICE_VOLUME_WITH_FLAGS -> "COMMAND_SET_DEVICE_VOLUME_WITH_FLAGS"
                    Player.COMMAND_ADJUST_DEVICE_VOLUME -> "COMMAND_ADJUST_DEVICE_VOLUME"
                    Player.COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS -> "COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS"
                    Player.COMMAND_SET_AUDIO_ATTRIBUTES -> "COMMAND_SET_AUDIO_ATTRIBUTES"
                    Player.COMMAND_SET_VIDEO_SURFACE -> "COMMAND_SET_VIDEO_SURFACE"
                    Player.COMMAND_GET_TEXT -> "COMMAND_GET_TEXT"
                    Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS -> "COMMAND_SET_TRACK_SELECTION_PARAMETERS"
                    Player.COMMAND_GET_TRACKS -> "COMMAND_GET_TRACKS"
                    Player.COMMAND_RELEASE -> "COMMAND_RELEASE"
                    else -> error("Unknown command $command")
                }

                add(commandName)
            }
        }.toString()
    }
}
