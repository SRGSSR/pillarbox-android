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
            else -> UNKNOWN
        }
    }
}
