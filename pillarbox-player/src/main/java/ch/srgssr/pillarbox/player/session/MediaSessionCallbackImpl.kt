/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession.Callback
import ch.srgssr.pillarbox.player.session.PillarboxSessionCommands.buildAvailableSessionCommands
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

internal open class MediaSessionCallbackImpl(
    val callback: Callback,
    val mediaSession: PillarboxMediaSession
) : MediaSession.Callback {

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
        val availableSessionCommands = session.buildAvailableSessionCommands()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(availableSessionCommands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        // TODO maybe add a way integrators can add custom commands
        DebugLogger.debug(TAG, "onCustomCommand ${customCommand.customAction} ${customCommand.customExtras} args = $args")
        val player = session.player
        if (player !is PillarboxPlayer) return Futures.immediateFailedFuture(UnsupportedOperationException())
        return when (customCommand.customAction) {
            PillarboxSessionCommands.ACTION_SMOOTH_SEEKING_ENABLED -> {
                handleCommandEnableSmoothSeeking(player, args)
            }

            PillarboxSessionCommands.ACTION_TRACKER_ENABLED -> {
                handleCommandEnableTracker(player, args)
            }

            PillarboxSessionCommands.ACTION_CURRENT_PLAYBACK_METRICS -> {
                handleCommandCurrentPlaybackMetrics(player)
            }

            PillarboxSessionCommands.ACTION_SEEK_PARAMETERS -> {
                handleCommandSeekParameters(player, args)
            }

            PillarboxSessionCommands.ACTION_ENABLE_IMAGE_OUTPUT -> {
                handleCommandEnableImageOutput(player, args, controller)
            }

            PillarboxSessionCommands.ACTION_CURRENT_PILLARBOX_METADATA -> {
                handleCommandGetPillarboxMetadata(player)
            }

            else -> {
                DebugLogger.warning(TAG, "Unsupported session command ${customCommand.customAction}")
                Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
            }
        }
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

    override fun onDisconnected(session: MediaSession, controller: MediaSession.ControllerInfo) {
        mediaSession.connectedControllersWithImageOutput.remove(controller)
    }

    private fun handleCommandEnableSmoothSeeking(player: PillarboxPlayer, args: Bundle): ListenableFuture<SessionResult> {
        if (args.containsKey(PillarboxSessionCommands.ARG_SMOOTH_SEEKING)) {
            player.smoothSeekingEnabled = args.getBoolean(PillarboxSessionCommands.ARG_SMOOTH_SEEKING)
        }
        return Futures.immediateFuture(
            SessionResult(
                SessionResult.RESULT_SUCCESS,
                Bundle().apply {
                    putBoolean(
                        PillarboxSessionCommands.ARG_SMOOTH_SEEKING,
                        player.smoothSeekingEnabled
                    )
                }
            )
        )
    }

    private fun handleCommandEnableTracker(player: PillarboxPlayer, args: Bundle): ListenableFuture<SessionResult> {
        if (args.containsKey(PillarboxSessionCommands.ARG_TRACKER_ENABLED)) {
            player.trackingEnabled = args.getBoolean(PillarboxSessionCommands.ARG_TRACKER_ENABLED)
        }
        return Futures.immediateFuture(
            SessionResult(
                SessionResult.RESULT_SUCCESS,
                Bundle().apply {
                    putBoolean(
                        PillarboxSessionCommands.ARG_TRACKER_ENABLED,
                        player.trackingEnabled
                    )
                }
            )
        )
    }

    private fun handleCommandEnableImageOutput(
        player: PillarboxPlayer,
        args: Bundle,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<SessionResult> {
        val enable = args.getBoolean(PillarboxSessionCommands.ARG_ENABLE_IMAGE_OUTPUT, false)
        if (player.isImageOutputAvailable) {
            if (enable) {
                mediaSession.connectedControllersWithImageOutput.add(controller)
                if (mediaSession.connectedControllersWithImageOutput.size == 1) {
                    player.setImageOutput(mediaSession.imageOutput)
                }
            } else {
                mediaSession.connectedControllersWithImageOutput.remove(controller)
                if (mediaSession.connectedControllersWithImageOutput.isEmpty()) {
                    player.setImageOutput(null)
                }
            }
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    private fun handleCommandSeekParameters(player: PillarboxPlayer, args: Bundle): ListenableFuture<SessionResult> {
        if (player.isSeekParametersAvailable &&
            args.containsKey(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE) &&
            args.containsKey(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_AFTER)
        ) {
            val toleranceBefore =
                args.getLong(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE, SeekParameters.DEFAULT.toleranceBeforeUs)
            val toleranceAfter =
                args.getLong(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_AFTER, SeekParameters.DEFAULT.toleranceAfterUs)
            player.setSeekParameters(SeekParameters(toleranceBefore, toleranceAfter))
        }
        return Futures.immediateFuture(
            SessionResult(
                SessionResult.RESULT_SUCCESS,
                Bundle().apply {
                    val seekParameters = player.getSeekParameters()
                    putLong(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_BEFORE, seekParameters.toleranceBeforeUs)
                    putLong(PillarboxSessionCommands.ARG_SEEK_PARAMETERS_TOLERANCE_AFTER, seekParameters.toleranceAfterUs)
                }
            )
        )
    }

    private fun handleCommandCurrentPlaybackMetrics(player: PillarboxPlayer): ListenableFuture<SessionResult> {
        val metrics = player.getCurrentMetrics()
        return Futures.immediateFuture(
            SessionResult(
                SessionResult.RESULT_SUCCESS,
                Bundle().apply {
                    putParcelable(
                        PillarboxSessionCommands.ARG_PLAYBACK_METRICS,
                        metrics
                    )
                }
            )
        )
    }

    private fun handleCommandGetPillarboxMetadata(
        player: PillarboxPlayer
    ): ListenableFuture<SessionResult> {
        val metadata = player.currentPillarboxMetadata
        return Futures.immediateFuture(
            SessionResult(
                SessionResult.RESULT_SUCCESS,
                Bundle().apply {
                    putParcelable(
                        PillarboxSessionCommands.ARG_PILLARBOX_META_DATA,
                        metadata
                    )
                }
            )
        )
    }

    private companion object {
        private const val TAG = "MediaSessionCallbackImpl"
    }
}
