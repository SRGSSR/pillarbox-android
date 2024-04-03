/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * PillarboxMediaSession link together a [MediaSession] to a [PillarboxPlayer].
 */
open class PillarboxMediaSession internal constructor() {

    interface Callback {

        fun onSetMediaItems(
            mediaSession: PillarboxMediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaItemsWithStartPosition> {
            return Util.transformFutureAsync(
                onAddMediaItems(mediaSession, controller, mediaItems)
            ) { input -> Futures.immediateFuture(MediaItemsWithStartPosition(input!!, startIndex, startPositionMs)) }
        }

        fun onAddMediaItems(
            mediaSession: PillarboxMediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            for (mediaItem in mediaItems) {
                if (mediaItem.localConfiguration == null) {
                    return Futures.immediateFailedFuture(UnsupportedOperationException())
                }
            }
            return Futures.immediateFuture(mediaItems)
        }

        object Default : Callback
    }

    class Builder(context: Context, player: PillarboxPlayer) {
        private val mediaSessionBuilder = MediaSession.Builder(context, player)
        private var callback: Callback = object : Callback {}

        fun setSessionActivity(pendingIntent: PendingIntent): Builder {
            mediaSessionBuilder.setSessionActivity(pendingIntent)
            return this
        }

        fun setId(id: String): Builder {
            mediaSessionBuilder.setId(id)
            return this
        }

        fun setCallback(callback: Callback) {
            this.callback = callback
        }

        fun build(): PillarboxMediaSession {
            val pillarboxMediaSession = PillarboxMediaSession()
            val media3SessionCallback = MediaSessionCallbackImpl(callback, pillarboxMediaSession)
            val mediaSession = mediaSessionBuilder
                .setCallback(media3SessionCallback)
                .build()
            pillarboxMediaSession.setMediaSession(mediaSession)
            return pillarboxMediaSession
        }
    }

    private lateinit var _mediaSession: MediaSession
    private val listener = ComponentListener()
    open val mediaSession: MediaSession
        get() {
            return _mediaSession
        }
    val player: PillarboxPlayer
        get() {
            return _mediaSession.player as PillarboxPlayer
        }

    private val playerSessionState: PlayerSessionState
        get() {
            return PlayerSessionState(player)
        }

    fun setPlayer(player: PillarboxPlayer) {
        if (player != this.player) {
            this.player.removeListener(listener)
            _mediaSession.player = player
            player.addListener(listener)
            for (controllerInfo in _mediaSession.connectedControllers) {
                _mediaSession.setSessionExtras(
                    controllerInfo,
                    playerSessionState.toBundle(_mediaSession.sessionExtras)
                )
            }
        }
    }

    internal fun setMediaSession(mediaSession: MediaSession) {
        this._mediaSession = mediaSession
        player.addListener(listener)
    }

    /**
     * Release the underlying [MediaSession]
     */
    fun release() {
        player.removeListener(listener)
        _mediaSession.release()
    }

    private inner class ComponentListener : PillarboxPlayer.Listener {

        private fun updateMediaSessionExtras() {
            for (controllerInfo in _mediaSession.connectedControllers) {
                _mediaSession.setSessionExtras(
                    controllerInfo,
                    playerSessionState.toBundle(_mediaSession.sessionExtras)
                )
            }
        }

        override fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean) {
            updateMediaSessionExtras()
        }

        override fun onTrackingEnabledChanged(trackingEnabled: Boolean) {
            updateMediaSessionExtras()
        }
    }

    internal open class MediaSessionCallbackImpl(
        val callback: Callback,
        val mediaSession: PillarboxMediaSession
    ) : MediaSession.Callback {

        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val availableSessionCommands = SessionCommands.Builder().apply {
                if (session is MediaLibraryService.MediaLibrarySession) {
                    addSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.commands)
                } else {
                    addSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.commands)
                }
                // TODO maybe add a way integrators can add custom commands
                add(PillarboxSessionCommands.COMMAND_SEEK_ENABLED)
                add(PillarboxSessionCommands.COMMAND_TRACKER_ENABLED)
            }.build()
            val pillarboxPlayer = session.player as PillarboxPlayer
            val playerSessionState = PlayerSessionState(pillarboxPlayer)
            DebugLogger.debug(TAG, "onConnect with state = $playerSessionState")
            val sessionExtras = playerSessionState.toBundle()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands)
                .setSessionExtras(sessionExtras)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            // TODO maybe add a way integrators can add custom commands
            DebugLogger.debug(TAG, "onCustomCommand ${customCommand.customAction} ${customCommand.customExtras}")
            when (customCommand.customAction) {
                PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED -> {
                    if (session.player is PillarboxPlayer) {
                        (session.player as PillarboxPlayer).smoothSeekingEnabled =
                            customCommand.customExtras.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }
            }
            DebugLogger.warning(TAG, "Unsupported session command ${customCommand.customAction}")
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaItemsWithStartPosition> {
            return callback.onSetMediaItems(this.mediaSession, controller, mediaItems, startIndex, startPositionMs)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return callback.onAddMediaItems(this.mediaSession, controller, mediaItems)
        }
    }

    companion object {
        internal const val TAG = "PillarboxMediaSession"
    }
}
