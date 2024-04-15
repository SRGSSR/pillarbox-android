/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import androidx.media3.session.SessionCommand

internal object PillarboxSessionCommands {
    const val SMOOTH_SEEKING_ARG = "pillarbox.smoothSeekingEnabled"
    const val TRACKER_ENABLED_ARG = "pillarbox.trackerEnabled"
    const val ARG_CHAPTER_CHANGED = "pillarbox.interval.chapter"
    const val ARG_BLOCKED_INTERVAL = "pillarbox.interval.blocked"

    const val CHAPTER_CHANGED = "pillarbox.chapter.changed"
    const val BLOCKED_INTERVAL_CHANGED = "pillarbox.blockedInterval.changed"
    const val SMOOTH_SEEKING_ENABLED = "pillarbox.smooth.seeking.enabled"
    const val TRACKER_ENABLED = "pillarbox.tracker.enabled"

    /**
     * Place holder command
     */
    val COMMAND_SMOOTH_SEEKING_ENABLED = SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle.EMPTY)

    /**
     * Place holder command
     */
    val COMMAND_TRACKER_ENABLED = SessionCommand(TRACKER_ENABLED, Bundle.EMPTY)

    val COMMAND_CHAPTER_CHANGED = SessionCommand(CHAPTER_CHANGED, Bundle.EMPTY)

    val COMMAND_BLOCK_INTERVAL_CHANGED = SessionCommand(BLOCKED_INTERVAL_CHANGED, Bundle.EMPTY)

    fun setSmoothSeekingEnabled(smoothSeekingEnabled: Boolean): SessionCommand {
        return SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle().apply { putBoolean(SMOOTH_SEEKING_ARG, smoothSeekingEnabled) })
    }

    fun setTrackerEnabled(trackerEnabled: Boolean): SessionCommand {
        return SessionCommand(TRACKER_ENABLED, Bundle().apply { putBoolean(TRACKER_ENABLED_ARG, trackerEnabled) })
    }
}
