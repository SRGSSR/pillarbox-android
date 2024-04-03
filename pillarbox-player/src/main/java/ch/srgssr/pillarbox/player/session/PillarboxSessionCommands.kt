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

    const val SMOOTH_SEEKING_ENABLED = "pillarbox.smooth.seeking.enabled"
    const val TRACKER_ENABLED = "pillarbox.tracker.enabled"

    /**
     * Place holder command
     */
    val COMMAND_SEEK_ENABLED = SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle.EMPTY)

    /**
     * Place holder command
     */
    val COMMAND_TRACKER_ENABLED = SessionCommand(TRACKER_ENABLED, Bundle.EMPTY)

    fun setSmoothSeekingCommand(smoothSeekingEnabled: Boolean) =
        SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle().apply { putBoolean(SMOOTH_SEEKING_ARG, smoothSeekingEnabled) })

    fun setTrackerEnabled(enabled: Boolean) =
        SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle().apply { putBoolean(TRACKER_ENABLED_ARG, enabled) })
}
