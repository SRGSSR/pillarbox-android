/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.tracker

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker

/**
 * Enable/Disable EventLogger when item is currently active.
 */
class SRGEventLoggerTracker : MediaItemTracker {
    private val eventLogger = EventLogger(TAG)

    override fun start(player: ExoPlayer) {
        Log.w(TAG, "---- Start")
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer) {
        Log.w(TAG, "---- Stop")
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
