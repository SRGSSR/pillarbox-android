/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.session.PillarboxSessionCommands
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Default media session callback that allow to add [MediaItem] with an url or an mediaId to the MediaController.
 *
 * @see [MediaSession.Builder.setCallback]
 */
class DefaultMediaSessionCallback : MediaSession.Callback {

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        for (mediaItem in mediaItems) {
            if (mediaItem.localConfiguration == null && mediaItem.mediaId.isBlank()) {
                return Futures.immediateFailedFuture(UnsupportedOperationException())
            }
        }
        return Futures.immediateFuture(mediaItems)
    }

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): ConnectionResult {
        val availableSessionCommands = SessionCommands.Builder().apply {
            if (session is MediaLibraryService.MediaLibrarySession) {
                addSessionCommands(ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.commands)
            } else {
                addSessionCommands(ConnectionResult.DEFAULT_SESSION_COMMANDS.commands)
            }
            add(PillarboxSessionCommands.COMMAND_SEEK_DISABLED)
            add(PillarboxSessionCommands.COMMAND_SEEK_ENABLED)
            add(PillarboxSessionCommands.COMMAND_SEEK_GET)
        }.build()
        return ConnectionResult.accept(availableSessionCommands, ConnectionResult.DEFAULT_PLAYER_COMMANDS)
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        DebugLogger.debug(TAG, "onCustomCommand ${customCommand.customAction} $args")
        when (customCommand.customAction) {
            PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED -> {
                if (session.player is PillarboxPlayer) {
                    (session.player as PillarboxPlayer).smoothSeekingEnabled = true
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }

            PillarboxSessionCommands.SMOOTH_SEEKING_DISABLED -> {
                if (session.player is PillarboxPlayer) {
                    (session.player as PillarboxPlayer).smoothSeekingEnabled = false
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }

            PillarboxSessionCommands.SMOOTH_SEEKING_GET -> {
                if (session.player is PillarboxPlayer) {
                    val state = (session.player as PillarboxPlayer).smoothSeekingEnabled
                    return Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            Bundle().apply {
                                putBoolean(
                                    "smoothSeekingEnabled",
                                    state
                                )
                            }
                        )
                    )
                }
            }
        }
        DebugLogger.warning(TAG, "Unsupported session command ${customCommand.customAction}")
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
    }

    companion object {
        private const val TAG = "PillarboxMediaSession"
    }
}
