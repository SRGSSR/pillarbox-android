/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * [PillarboxMediaSession] link together a [MediaSession] to a [PillarboxPlayer].
 */
open class PillarboxMediaSession internal constructor() {

    /**
     * Callback
     */
    interface Callback {

        /**
         * @see MediaSession.Callback.onSetMediaItems
         */
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

        /**
         * @see MediaSession.Callback.onAddMediaItems
         */
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

        /**
         * Default implementation
         */
        object Default : Callback
    }

    /**
     * Builder
     *
     * @param context
     * @param player
     */
    class Builder(context: Context, player: PillarboxPlayer) {
        private val mediaSessionBuilder = MediaSession.Builder(context, player)
        private var callback: Callback = Callback.Default

        /**
         * Sets a [PendingIntent] to launch an [Activity][android.app.Activity] for the [MediaSession].
         * This can be used as a quick link to an ongoing media screen.
         *
         * @param pendingIntent The [PendingIntent].
         * @return this builder for convenience.
         * @see MediaSession.Builder.setSessionActivity
         */
        fun setSessionActivity(pendingIntent: PendingIntent): Builder {
            mediaSessionBuilder.setSessionActivity(pendingIntent)
            return this
        }

        /**
         * Sets an ID of the [PillarboxMediaSession]. If not set, an empty string will be used.
         * Use this if and only if your app supports multiple playback at the same time and also wants to provide external apps to have
         * finer-grained controls.
         *
         * @param id The ID. Must be unique among all sessions per package.
         * @return this builder for convenience.
         * @see MediaSession.Builder.setId
         */
        fun setId(id: String): Builder {
            mediaSessionBuilder.setId(id)
            return this
        }

        /**
         * Set callback
         *
         * @param callback
         */
        fun setCallback(callback: Callback) {
            this.callback = callback
        }

        /**
         * Build
         *
         * @return create a [PillarboxMediaSession].
         */
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

    /**
     * The underlying [androidx.media3.session.MediaSession].
     */
    open val mediaSession: MediaSession
        get() {
            return _mediaSession
        }

    /**
     * @see MediaSession.getSessionCompatToken
     */
    val token: MediaSessionCompat.Token
        get() {
            return _mediaSession.sessionCompatToken
        }

    /**
     * Player
     */
    var player: PillarboxPlayer
        get() {
            return _mediaSession.player as PillarboxPlayer
        }
        set(value) {
            if (value != this.player) {
                this.player.removeListener(listener)
                _mediaSession.player = value
                value.addListener(listener)
                listener.updateMediaSessionExtras()
            }
        }

    private val playerSessionState: PlayerSessionState
        get() {
            return PlayerSessionState(player)
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

        fun updateMediaSessionExtras() {
            for (controllerInfo in _mediaSession.connectedControllers) {
                _mediaSession.setSessionExtras(
                    controllerInfo,
                    playerSessionState.toBundle(_mediaSession.sessionExtras)
                )
            }
        }

        override fun onChapterChanged(chapter: Chapter?) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_CHAPTER_CHANGED, chapter)
            }
            _mediaSession.connectedControllers.forEach {
                Log.d(TAG, "onChapterChanged $chapter")
                _mediaSession.sendCustomCommand(it, PillarboxSessionCommands.COMMAND_CHAPTER_CHANGED, commandArg)
            }
        }

        override fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_BLOCKED, blockedTimeRange)
            }
            _mediaSession.connectedControllers.forEach {
                _mediaSession.sendCustomCommand(it, PillarboxSessionCommands.COMMAND_BLOCKED_CHANGED, commandArg)
            }
        }

        override fun onCreditChanged(credit: Credit?) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_CREDIT, credit)
            }
            _mediaSession.connectedControllers.forEach {
                Log.d("TAG", "onCreditChanged $credit")
                _mediaSession.sendCustomCommand(it, PillarboxSessionCommands.COMMAND_CREDIT_CHANGED, commandArg)
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
                add(PillarboxSessionCommands.COMMAND_SMOOTH_SEEKING_ENABLED)
                add(PillarboxSessionCommands.COMMAND_TRACKER_ENABLED)
                add(PillarboxSessionCommands.COMMAND_CHAPTER_CHANGED)
                add(PillarboxSessionCommands.COMMAND_BLOCKED_CHANGED)
                add(PillarboxSessionCommands.COMMAND_CREDIT_CHANGED)
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

        @Suppress("ReturnCount")
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            // TODO maybe add a way integrators can add custom commands
            DebugLogger.debug(TAG, "onCustomCommand ${customCommand.customAction} ${customCommand.customExtras}")
            val player = session.player
            when (customCommand.customAction) {
                PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED -> {
                    if (player is PillarboxPlayer) {
                        player.smoothSeekingEnabled = customCommand.customExtras.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                PillarboxSessionCommands.TRACKER_ENABLED -> {
                    if (player is PillarboxPlayer) {
                        player.trackingEnabled = customCommand.customExtras.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG)
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
