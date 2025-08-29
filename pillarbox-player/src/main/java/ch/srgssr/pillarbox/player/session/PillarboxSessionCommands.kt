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
    const val ARG_CHAPTER = "pillarbox.chapter"
    const val ARG_BLOCKED = "pillarbox.blocked"
    const val ARG_CREDIT = "pillarbox.credit"
    const val ARG_PLAYBACK_METRICS = "pillarbox.playbackMetrics"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE = "pillarbox.seek.parameters.toleranceBefore"
    const val ARG_SEEK_PARAMETERS_TOLERANCE_AFTER = "pillarbox.seek.parameters.toleranceAfter"
    const val ARG_PILLARBOX_METADATA = "pillarbox.metadata"
    const val ARG_ENABLE_IMAGE_OUTPUT = "pillarbox.enable.image.output"
    const val ARG_BITMAP = "pillarbox.bitmap"
    const val ARG_PRESENTATION_TIME = "pillarbox.presentation.time"

    val COMMAND_CHAPTER_CHANGED = SessionCommand("pillarbox.chapter.changed", Bundle.EMPTY)
    val COMMAND_BLOCKED_CHANGED = SessionCommand("pillarbox.blockedTimeRange.changed", Bundle.EMPTY)
    val COMMAND_CREDIT_CHANGED = SessionCommand("pillarbox.credit.changed", Bundle.EMPTY)
    val COMMAND_PILLARBOX_METADATA_CHANGED = SessionCommand("pillarbox.pillarboxMetadata.changed", Bundle.EMPTY)
    val COMMAND_TRACKING_ENABLED_CHANGED = SessionCommand("pillarbox.trackingEnabled.changed", Bundle.EMPTY)
    val COMMAND_SMOOTH_SEEKING_ENABLED_CHANGED = SessionCommand("pillarbox.smoothSeekingEnabled.changed", Bundle.EMPTY)
    val COMMAND_IMAGE_OUTPUT_DATA_CHANGED = SessionCommand("pillarbox.imageOutputData.changed", Bundle.EMPTY)
    val COMMAND_GET_CURRENT_PLAYBACK_METRICS = SessionCommand("pillarbox.getCurrentPlaybackMetrics", Bundle.EMPTY)
    val COMMAND_GET_CURRENT_PILLARBOX_METADATA = SessionCommand("pillarbox.getCurrentPillarboxMetadata", Bundle.EMPTY)
    val COMMAND_GET_SMOOTH_SEEKING_ENABLED = SessionCommand("pillarbox.getSmoothSeekingEnabled", Bundle.EMPTY)
    val COMMAND_SET_SMOOTH_SEEKING_ENABLED = SessionCommand("pillarbox.setSmoothSeekingEnabled", Bundle.EMPTY)
    val COMMAND_GET_TRACKER_ENABLED = SessionCommand("pillarbox.getTrackingEnabled", Bundle.EMPTY)
    val COMMAND_SET_TRACKER_ENABLED = SessionCommand("pillarbox.setTrackingEnabled", Bundle.EMPTY)
    val COMMAND_GET_SEEK_PARAMETERS = SessionCommand("pillarbox.getSeekParameters", Bundle.EMPTY)
    val COMMAND_SET_SEEK_PARAMETERS = SessionCommand("pillarbox.setSeekParameters", Bundle.EMPTY)
    val COMMAND_ENABLE_IMAGE_OUTPUT = SessionCommand("pillarbox.enableImageOutput", Bundle.EMPTY)

    val AVAILABLE_COMMANDS = listOf(
        COMMAND_SET_TRACKER_ENABLED,
        COMMAND_GET_TRACKER_ENABLED,
        COMMAND_SET_SMOOTH_SEEKING_ENABLED,
        COMMAND_GET_SMOOTH_SEEKING_ENABLED,
        COMMAND_GET_CURRENT_PILLARBOX_METADATA,
        COMMAND_CHAPTER_CHANGED,
        COMMAND_CREDIT_CHANGED,
        COMMAND_BLOCKED_CHANGED,
        COMMAND_PILLARBOX_METADATA_CHANGED,
        COMMAND_TRACKING_ENABLED_CHANGED,
        COMMAND_SMOOTH_SEEKING_ENABLED_CHANGED,
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
                    listCustomCommand.add(COMMAND_SET_SEEK_PARAMETERS)
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
