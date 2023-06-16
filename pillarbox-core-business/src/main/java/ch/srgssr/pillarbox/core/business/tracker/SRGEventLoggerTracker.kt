/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import kotlin.time.Duration.Companion.milliseconds

/**
 * Enable/Disable EventLogger when item is currently active.
 */
class SRGEventLoggerTracker : MediaItemTracker {
    private val eventLogger = EventLogger(TAG)

    override fun start(player: ExoPlayer, initialData: Any?) {
        Log.w(TAG, "---- Start")
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        Log.w(TAG, "---- Stop because $reason at ${positionMs.milliseconds}")
        player.removeAnalyticsListener(eventLogger)
    }

    override fun update(data: Any) {
        Log.w(TAG, "---- Update data = $data")
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
