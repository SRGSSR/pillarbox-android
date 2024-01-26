/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Total playtime counter.
 *
 * @param timeProvider A callback invoked whenever the current time is needed.
 */
class TotalPlaytimeCounter(
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
) {
    private var totalPlayTime: Duration = Duration.ZERO
    private var lastPlayTime = 0L

    /**
     * Play
     * Calling twice play after sometime will compute totalPlaytime
     */
    fun play() {
        pause()
        lastPlayTime = timeProvider()
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
            totalPlayTime + (timeProvider() - lastPlayTime).milliseconds
        }
    }

    /**
     * Pause total play time tracking and compute total playtime.
     */
    fun pause() {
        if (lastPlayTime > 0L) {
            totalPlayTime += (timeProvider() - lastPlayTime).milliseconds
            lastPlayTime = 0L
        }
    }
}
