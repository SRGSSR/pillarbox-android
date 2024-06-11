/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger
import kotlin.time.Duration.Companion.milliseconds

/**
 * Enable/Disable EventLogger when item is currently active.
 */
class SRGEventLoggerTracker : MediaItemTracker {
    private val eventLogger = PillarboxEventLogger(TAG)

    override fun start(player: ExoPlayer, initialData: Any?) {
        Log.w(TAG, "---- Start")
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        Log.w(TAG, "---- Stop because $reason at ${positionMs.milliseconds}")
        player.removeAnalyticsListener(eventLogger)
    }

    /**
     * Factory for a [SRGEventLoggerTracker]
     */
    class Factory : MediaItemTracker.Factory {

        override fun create(): MediaItemTracker {
            return SRGEventLoggerTracker()
        }
    }

    companion object {
        private const val TAG = "SRGLogger"
    }
}
