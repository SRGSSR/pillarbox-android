/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.test.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.LiveConfiguration
import androidx.media3.common.Timeline
import androidx.media3.common.util.Util

class TestTimeline(
    private val mediaItem: MediaItem = MediaItem.Builder().setMediaId("test").build(),
    private val duration: Long = 30_000,
    private val presentationStartTimeMs: Long = 0,
    private val windowStartTimeMs: Long = 0,
    private val elapsedRealtimeEpochOffsetMs: Long = 0,
    private val isSeekable: Boolean = true,
    private val isDynamic: Boolean = false,
    isLive: Boolean = false,
) : Timeline() {
    private val liveConfiguration: LiveConfiguration? = if (isLive) {
        LiveConfiguration.Builder().build()
    } else {
        null
    }

    override fun getWindowCount(): Int {
        return 1
    }

    override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
        window.set(
            Any(),
            mediaItem,
            null,
            presentationStartTimeMs,
            windowStartTimeMs,
            elapsedRealtimeEpochOffsetMs,
            isSeekable,
            isDynamic,
            liveConfiguration,
            0,
            Util.msToUs(duration),
            0,
            0,
            0

        )
        return window
    }

    override fun getPeriodCount(): Int {
        return 1
    }

    override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean): Period {
        return period
    }

    override fun getIndexOfPeriod(uid: Any): Int {
        return 0
    }

    override fun getUidOfPeriod(periodIndex: Int): Any {
        return Any()
    }
}
