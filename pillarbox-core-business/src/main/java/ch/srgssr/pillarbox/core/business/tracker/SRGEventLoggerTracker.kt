/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger

/**
 * A [MediaItemTracker] that enables and disables the [PillarboxEventLogger] for the currently active [MediaItem].
 */
class SRGEventLoggerTracker : MediaItemTracker<Unit> {
    private val eventLogger = PillarboxEventLogger(TAG)

    override fun start(player: PillarboxExoPlayer, data: Unit) {
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: PillarboxExoPlayer) {
        player.removeAnalyticsListener(eventLogger)
    }

    /**
     * A factory class responsible for creating instances of [SRGEventLoggerTracker].
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
