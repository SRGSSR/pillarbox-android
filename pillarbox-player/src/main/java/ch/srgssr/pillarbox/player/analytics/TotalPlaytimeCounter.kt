/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * Total playtime counter.
 *
 * @param timeProvider A callback invoked whenever the current time is needed.
 */
class TotalPlaytimeCounter internal constructor(
    private val timeProvider: () -> Long,
) {
    private var totalPlayTime: Duration = ZERO
    private var lastPlayTime = 0L

    constructor() : this(
        timeProvider = { System.currentTimeMillis() },
    )

    /**
     * Reset total playtime to zero
     */
    fun reset() {
        totalPlayTime = ZERO
        lastPlayTime = 0L
    }

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
