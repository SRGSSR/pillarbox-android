/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import ch.srgssr.pillarbox.player.PillarboxPlayer

internal object PillarboxSessionCommands {
    const val ARG_SMOOTH_SEEKING = "pillarbox.smoothSeekingEnabled"
    const val ARG_TRACKER_ENABLED = "pillarbox.trackerEnabled"
    const val ARG_CHAPTER_CHANGED = "pillarbox.range.chapter"
    const val ARG_BLOCKED = "pillarbox.range.blocked"
    const val ARG_CREDIT = "pillarbox.range.credit"
    const val ARG_PLAYBACK_METRICS = "pillarbox.playback.metrics"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE = "pillarbox.seek.parameters.toleranceBefore"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_AFTER = "pillarbox.seek.parameters.toleranceAfter"
    const val ARG_PILLARBOX_META_DATA = "pillarbox.metadata"
    const val ARG_ENABLE_IMAGE_OUTPUT = "pillarbox.enable.image.output"
    const val ARG_BITMAP = "pillarbox.bitmap"
    const val ARG_PRESENTATION_TIME = "pillarbox.presentation.time"

    const val ACTION_CHAPTER_CHANGED = "pillarbox.chapter.changed"
    const val ACTION_BLOCKED_CHANGED = "pillarbox.blocked.changed"
    const val ACTION_CREDIT_CHANGED = "pillarbox.credit.changed"
    const val ACTION_SMOOTH_SEEKING_ENABLED = "pillarbox.smooth.seeking.enabled"
    const val ACTION_TRACKER_ENABLED = "pillarbox.tracker.enabled"
    const val ACTION_CURRENT_PLAYBACK_METRICS = "pillarbox.current.playback.metrics"
    const val ACTION_SEEK_PARAMETERS = "pillarbox.seek.parameters"
    const val ACTION_ENABLE_IMAGE_OUTPUT = "pillarbox.image.output"
    const val ACTION_IMAGE_OUTPUT_CHANGED = "pillarbox.image.output.changed"
    const val ACTION_CURRENT_PILLARBOX_METADATA = "pillarbox.current.metadata"

    val COMMAND_CHAPTER_CHANGED = SessionCommand(ACTION_CHAPTER_CHANGED, Bundle.EMPTY)
    val COMMAND_BLOCKED_CHANGED = SessionCommand(ACTION_BLOCKED_CHANGED, Bundle.EMPTY)
    val COMMAND_CREDIT_CHANGED = SessionCommand(ACTION_CREDIT_CHANGED, Bundle.EMPTY)
    val COMMAND_GET_CURRENT_PLAYBACK_METRICS = SessionCommand(ACTION_CURRENT_PLAYBACK_METRICS, Bundle.EMPTY)
    val COMMAND_SMOOTH_SEEKING_ENABLED = SessionCommand(ACTION_SMOOTH_SEEKING_ENABLED, Bundle.EMPTY)
    val COMMAND_TRACKER_ENABLED = SessionCommand(ACTION_TRACKER_ENABLED, Bundle.EMPTY)
    val COMMAND_GET_SEEK_PARAMETERS = SessionCommand(ACTION_SEEK_PARAMETERS, Bundle.EMPTY)
    val COMMAND_ENABLE_IMAGE_OUTPUT = SessionCommand(ACTION_ENABLE_IMAGE_OUTPUT, Bundle.EMPTY)
    val COMMAND_IMAGE_OUTPUT_DATA_CHANGED = SessionCommand(ACTION_IMAGE_OUTPUT_CHANGED, Bundle.EMPTY)

    val COMMAND_GET_CURRENT_PILLARBOX_METADATA = SessionCommand(ACTION_CURRENT_PILLARBOX_METADATA, Bundle.EMPTY)

    val AVAILABLE_COMMANDS = listOf(
        COMMAND_SMOOTH_SEEKING_ENABLED,
        COMMAND_TRACKER_ENABLED,
        COMMAND_CHAPTER_CHANGED,
        COMMAND_CREDIT_CHANGED,
        COMMAND_BLOCKED_CHANGED,
        COMMAND_GET_CURRENT_PILLARBOX_METADATA,
    )

    fun MediaSession.buildAvailableSessionCommands(): SessionCommands {
        val sessionCommandsBuilder = if (this is MediaLibraryService.MediaLibrarySession) {
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
        } else {
            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
        }.apply {
            val player = player
            if (player is PillarboxPlayer) {
                val listCustomCommand = mutableListOf<SessionCommand>()
                addSessionCommands(AVAILABLE_COMMANDS)
                if (player.isSeekParametersAvailable) {
                    listCustomCommand.add(COMMAND_GET_SEEK_PARAMETERS)
                }
                if (player.isImageOutputAvailable) {
                    listCustomCommand.add(COMMAND_ENABLE_IMAGE_OUTPUT)
                    listCustomCommand.add(COMMAND_IMAGE_OUTPUT_DATA_CHANGED)
                }
                if (player.isMetricsAvailable) {
                    listCustomCommand.add(COMMAND_GET_CURRENT_PLAYBACK_METRICS)
                }
                addSessionCommands(listCustomCommand)
            }
        }
        return sessionCommandsBuilder.build()
    }
}
