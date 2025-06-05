/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.Player
import com.google.android.gms.cast.MediaStatus

/**
 * Utils to convert GoogleCast types to Media3 types.
 */
object PillarboxCastUtil {

    /**
     * Transform a GoogleCast repeatMode to a Media3 repeatMode.
     */
    fun getRepeatModeFromQueueRepeatMode(queueRepeatMode: Int?): @Player.RepeatMode Int {
        return when (queueRepeatMode) {
            MediaStatus.REPEAT_MODE_REPEAT_ALL,
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE -> Player.REPEAT_MODE_ALL

            MediaStatus.REPEAT_MODE_REPEAT_OFF -> Player.REPEAT_MODE_OFF
            MediaStatus.REPEAT_MODE_REPEAT_SINGLE -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    /**
     * Transform a Media3 repeatMode to and GoogleCast repeatMode.
     */
    fun getQueueRepeatModeFromRepeatMode(repeatMode: @Player.RepeatMode Int): Int {
        return when (repeatMode) {
            Player.REPEAT_MODE_OFF -> MediaStatus.REPEAT_MODE_REPEAT_OFF
            Player.REPEAT_MODE_ONE -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE
            Player.REPEAT_MODE_ALL -> MediaStatus.REPEAT_MODE_REPEAT_ALL
            else -> MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
    }
}
