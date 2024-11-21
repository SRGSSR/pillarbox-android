/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * A class for tracking the total playtime of something.
 *
 * @param timeProvider A function that returns the current time, in milliseconds.
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
     * Resets the total playtime counter to zero.
     */
    fun reset() {
        totalPlayTime = ZERO
        lastPlayTime = 0L
    }

    /**
     * Starts or resumes playback.
     *
     * Calling this function after a previous call to [play] will automatically calculate the accumulated playtime.
     */
    fun play() {
        pause()
        lastPlayTime = timeProvider()
    }

    /**
     * Calculates the total play time.
     *
     * @return Either the accumulated total play time if paused, or the total play time plus the time elapsed since the last call to [play] if
     * currently playing.
     */
    fun getTotalPlayTime(): Duration {
        return if (lastPlayTime <= 0L) {
            totalPlayTime
        } else {
            totalPlayTime + (timeProvider() - lastPlayTime).milliseconds
        }
    }

    /**
     * Pauses the tracking of total play time.
     */
    fun pause() {
        if (lastPlayTime > 0L) {
            totalPlayTime += (timeProvider() - lastPlayTime).milliseconds
            lastPlayTime = 0L
        }
    }
}
