/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import androidx.media3.session.SessionCommand

internal object PillarboxSessionCommands {
    const val ARG_SMOOTH_SEEKING = "pillarbox.smoothSeekingEnabled"
    const val ARG_TRACKER_ENABLED = "pillarbox.trackerEnabled"
    const val ARG_CHAPTER_CHANGED = "pillarbox.range.chapter"
    const val ARG_BLOCKED = "pillarbox.range.blocked"
    const val ARG_CREDIT = "pillarbox.range.credit"
    const val ARG_PLAYBACK_METRICS = "pillarbox.playback.metrics"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE = "pillarbox.seek.parameters.toleranceBefore"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_AFTER = "pillarbox.seek.parameters.toleranceAfter"

    const val ACTION_CHAPTER_CHANGED = "pillarbox.chapter.changed"
    const val ACTION_BLOCKED_CHANGED = "pillarbox.blocked.changed"
    const val ACTION_CREDIT_CHANGED = "pillarbox.credit.changed"
    const val ACTION_SMOOTH_SEEKING_ENABLED = "pillarbox.smooth.seeking.enabled"
    const val ACTION_TRACKER_ENABLED = "pillarbox.tracker.enabled"
    const val ACTION_CURRENT_PLAYBACK_METRICS = "pillarbox.current.playback.metrics"
    const val ACTION_SEEK_PARAMETERS = "pillarbox.seek.parameters"

    val COMMAND_CHAPTER_CHANGED = SessionCommand(ACTION_CHAPTER_CHANGED, Bundle.EMPTY)
    val COMMAND_BLOCKED_CHANGED = SessionCommand(ACTION_BLOCKED_CHANGED, Bundle.EMPTY)
    val COMMAND_CREDIT_CHANGED = SessionCommand(ACTION_CREDIT_CHANGED, Bundle.EMPTY)
    val COMMAND_GET_CURRENT_PLAYBACK_METRICS = SessionCommand(ACTION_CURRENT_PLAYBACK_METRICS, Bundle.EMPTY)
    val COMMAND_SMOOTH_SEEKING_ENABLED = SessionCommand(ACTION_SMOOTH_SEEKING_ENABLED, Bundle.EMPTY)
    val COMMAND_TRACKER_ENABLED = SessionCommand(ACTION_TRACKER_ENABLED, Bundle.EMPTY)
    val COMMAND_GET_SEEK_PARAMETERS = SessionCommand(ACTION_SEEK_PARAMETERS, Bundle.EMPTY)

    val AVAILABLE_COMMANDS = listOf(
        COMMAND_SMOOTH_SEEKING_ENABLED,
        COMMAND_TRACKER_ENABLED,
        COMMAND_CHAPTER_CHANGED,
        COMMAND_CREDIT_CHANGED,
        COMMAND_BLOCKED_CHANGED,
        COMMAND_GET_CURRENT_PLAYBACK_METRICS,
    )
}
