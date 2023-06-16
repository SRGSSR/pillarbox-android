/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Total playtime counter
 */
class TotalPlaytimeCounter {
    private var totalPlayTime: Duration = Duration.ZERO
    private var lastPlayTime = 0L

    /**
     * Play
     * Calling twice play after sometime will compute totalPlaytime
     */
    fun play() {
        pause()
        lastPlayTime = System.currentTimeMillis()
    }

    /**
     * Get total play time
     *
     * @return if paused totalPlayTime else totalPlayTime + delta from last play
     */
    fun getTotalPlayTime(): Duration {
        return if (lastPlayTime <= 0L) {
            totalPlayTime
        } else {
            totalPlayTime + (System.currentTimeMillis() - lastPlayTime).milliseconds
        }
    }

    /**
     * Pause total play time tracking and compute total playtime.
     */
    fun pause() {
        if (lastPlayTime > 0L) {
            totalPlayTime += (System.currentTimeMillis() - lastPlayTime).milliseconds
            lastPlayTime = 0L
        }
    }
}
