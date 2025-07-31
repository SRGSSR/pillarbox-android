/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.session.PillarboxSessionCommands.buildAvailableSessionCommands
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
         * @return this builder for convenience.
         */
        fun setCallback(callback: Callback): Builder {
            this.callback = callback
            return this
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

    internal val imageOutput: ImageOutput = ImageOutputImpl()
    internal val connectedControllersWithImageOutput = mutableListOf<MediaSession.ControllerInfo>()

    /**
     * The underlying [androidx.media3.session.MediaSession].
     */
    open val mediaSession: MediaSession
        get() {
            return _mediaSession
        }

    /**
     * @see MediaSession.getPlatformToken
     */
    val token: android.media.session.MediaSession.Token
        get() {
            return _mediaSession.platformToken
        }

    /**
     * Player
     */
    var player: PillarboxPlayer
        get() {
            return _mediaSession.player as PillarboxPlayer
        }
        set(value) {
            val player = _mediaSession.player as PillarboxPlayer
            if (value != player) {
                player.setImageOutput(null)
                player.removeListener(listener)
                _mediaSession.player = value
                _mediaSession.connectedControllers.forEach {
                    _mediaSession.setAvailableCommands(
                        it,
                        _mediaSession.buildAvailableSessionCommands(),
                        MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                    )
                }
                value.addListener(listener)
                if (value.isImageOutputAvailable && connectedControllersWithImageOutput.isNotEmpty()) {
                    value.setImageOutput(imageOutput)
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
        player.setImageOutput(null)
        player.removeListener(listener)
        _mediaSession.release()
    }

    private inner class ComponentListener : PillarboxPlayer.Listener {

        override fun onChapterChanged(chapter: Chapter?) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_CHAPTER, chapter)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_CHAPTER_CHANGED, commandArg)
        }

        override fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_BLOCKED, blockedTimeRange)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_BLOCKED_CHANGED, commandArg)
        }

        override fun onCreditChanged(credit: Credit?) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_CREDIT, credit)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_CREDIT_CHANGED, commandArg)
        }

        override fun onPillarboxMetadataChanged(pillarboxMetadata: PillarboxMetadata) {
            val commandArg = Bundle().apply {
                putParcelable(PillarboxSessionCommands.ARG_PILLARBOX_META_DATA, pillarboxMetadata)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_PILLARBOX_METADATA_CHANGED, commandArg)
        }

        override fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean) {
            val commandArg = Bundle().apply {
                putBoolean(PillarboxSessionCommands.ARG_SMOOTH_SEEKING, smoothSeekingEnabled)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_SMOOTH_SEEKING_ENABLED_CHANGED, commandArg)
        }

        override fun onTrackingEnabledChanged(trackingEnabled: Boolean) {
            val commandArg = Bundle().apply {
                putBoolean(PillarboxSessionCommands.ARG_TRACKER_ENABLED, trackingEnabled)
            }
            _mediaSession.broadcastCustomCommand(PillarboxSessionCommands.COMMAND_TRACKING_ENABLED_CHANGED, commandArg)
        }
    }

    private inner class ImageOutputImpl : ImageOutput {

        override fun onImageAvailable(presentationTimeUs: Long, bitmap: Bitmap) {
            val args = Bundle().apply {
                putLong(PillarboxSessionCommands.ARG_PRESENTATION_TIME, presentationTimeUs)
                putParcelable(PillarboxSessionCommands.ARG_BITMAP, bitmap)
            }
            connectedControllersWithImageOutput.forEach {
                _mediaSession.sendCustomCommand(it, PillarboxSessionCommands.COMMAND_IMAGE_OUTPUT_DATA_CHANGED, args)
            }
        }

        override fun onDisabled() {
            connectedControllersWithImageOutput.forEach {
                _mediaSession.sendCustomCommand(it, PillarboxSessionCommands.COMMAND_IMAGE_OUTPUT_DATA_CHANGED, Bundle.EMPTY)
            }
        }
    }
}
