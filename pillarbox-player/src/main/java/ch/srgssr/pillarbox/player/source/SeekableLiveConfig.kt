/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.C

/**
 * Seekable live configuration
 *
 * To enable default Media3 behavior use [MEDIA3_DEFAULT_CONFIG].
 *
 * @property minHlsChunkCount  The minimal chunk count to allow seeking. By default [DEFAULT_MIN_HLS_CHUNK_COUNT].
 * @property minDashTimeShiftMs The minimal time shift duration to allow seeking. By default [DEFAULT_MIN_DASH_TIME_SHIFT_MS].
 */
data class SeekableLiveConfig(
    val minHlsChunkCount: Int = DEFAULT_MIN_HLS_CHUNK_COUNT,
    val minDashTimeShiftMs: Long = DEFAULT_MIN_DASH_TIME_SHIFT_MS,
) {

    companion object {
        /**
         * Default min HLS chunk count matching iOS AVPlayer.
         */
        const val DEFAULT_MIN_HLS_CHUNK_COUNT = 3

        /**
         * Default min DASH time shift for live streams.
         */
        const val DEFAULT_MIN_DASH_TIME_SHIFT_MS = 30_000L

        /**
         * Live config like it is done by Media3
         */
        val MEDIA3_DEFAULT_CONFIG = SeekableLiveConfig(minHlsChunkCount = 0, minDashTimeShiftMs = C.TIME_UNSET)
    }
}
