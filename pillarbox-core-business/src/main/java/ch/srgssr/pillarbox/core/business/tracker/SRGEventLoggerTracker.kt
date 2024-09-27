/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger

/**
 * Enable/Disable EventLogger when item is currently active.
 */
class SRGEventLoggerTracker : MediaItemTracker<Unit> {
    private val eventLogger = PillarboxEventLogger(TAG)

    override fun start(player: ExoPlayer, data: Unit) {
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer) {
        player.removeAnalyticsListener(eventLogger)
    }

    /**
     * Factory for a [SRGEventLoggerTracker]
     */
    class Factory : MediaItemTracker.Factory<Unit> {

        override fun create(): MediaItemTracker<Unit> {
            return SRGEventLoggerTracker()
        }
    }

    private companion object {
        private const val TAG = "SRGLogger"
    }
}
