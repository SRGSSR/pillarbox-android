/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.extension.setPreferredAudioRoleFlagsToAccessibilityManagerSettings
import ch.srgssr.pillarbox.player.extension.setSeekIncrements
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository

/**
 * Pillarbox player
 *
 * @param exoPlayer
 * @param mediaItemTrackerProvider
 *
 * @constructor
 */
class PillarboxPlayer internal constructor(
    private val exoPlayer: ExoPlayer,
    val loadControl: PillarboxLoadControl,
    mediaItemTrackerProvider: MediaItemTrackerProvider?
) :
    ExoPlayer by exoPlayer, PillarboxExoPlayer {
    private val listeners = HashSet<PillarboxExoPlayer.Listener>()
    private val itemTracker: CurrentMediaItemTracker?
    private val window = Window()
    override var smoothSeekingEnabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) {
                    seekEnd()
                }
                clearSeeking()
                val listeners = HashSet(listeners)
                for (listener in listeners) {
                    listener.onSmoothSeekingEnabledChanged(value)
                }
            }
        }
    private var pendingSeek: Long? = null
    private var isSeeking: Boolean = false
    private var lastSeekTime = 0L

    /**
     * Enable or disable MediaItem tracking
     */
    var trackingEnabled: Boolean
        set(value) = itemTracker?.let { it.enabled = value } ?: Unit
        get() = itemTracker?.enabled ?: false

    init {
        exoPlayer.addListener(ComponentListener())
        itemTracker = mediaItemTrackerProvider?.let {
            CurrentMediaItemTracker(this, it)
        }
        if (BuildConfig.DEBUG) {
            addAnalyticsListener(EventLogger())
        }
    }

    constructor(
        context: Context,
        mediaItemSource: MediaItemSource,
        dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory(),
        loadControl: PillarboxLoadControl = PillarboxLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository(),
        seekIncrement: SeekIncrement = SeekIncrement()
    ) : this(
        ExoPlayer.Builder(context)
            .setUsePlatformDiagnostics(false)
            .setSeekIncrements(seekIncrement)
            .setRenderersFactory(
                DefaultRenderersFactory(context)
                    .forceEnableMediaCodecAsynchronousQueueing()
                    .experimentalSetSynchronizeCodecInteractionsWithQueueingEnabled(true)
                // .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                // .setEnableDecoderFallback(true)
            )
            .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                PillarboxMediaSourceFactory(
                    mediaItemSource = mediaItemSource,
                    defaultMediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
                )
            )
            .setTrackSelector(
                DefaultTrackSelector(
                    context,
                    TrackSelectionParameters.Builder(context)
                        .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
                        .build()
                )
            )
            .setDeviceVolumeControlEnabled(true) // allow player to control device volume
            .build(),
        mediaItemTrackerProvider = mediaItemTrackerProvider, loadControl = loadControl
    )

    override fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
        if (listener is PillarboxExoPlayer.Listener) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: Player.Listener) {
        exoPlayer.removeListener(listener)
        if (listener is PillarboxExoPlayer.Listener) {
            listeners.remove(listener)
        }
        exoPlayer.isPlaying
    }

    override fun seekTo(positionMs: Long) {
        if (!smoothSeekingEnabled) {
            exoPlayer.seekTo(positionMs)
            return
        }
        smoothSeekTo(positionMs)
    }

    private fun smoothSeekTo(positionMs: Long) {
        if (isSeeking) {
            pendingSeek = positionMs
            return
        }
        isSeeking = true
        lastSeekTime = System.currentTimeMillis()
        exoPlayer.seekTo(positionMs)
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        if (!smoothSeekingEnabled) {
            exoPlayer.seekTo(mediaItemIndex, positionMs)
            return
        }
        smoothSeekTo(mediaItemIndex, positionMs)
    }

    private fun smoothSeekTo(mediaItemIndex: Int, positionMs: Long) {
        if (mediaItemIndex != currentMediaItemIndex) {
            clearSeeking()
            exoPlayer.seekTo(mediaItemIndex, positionMs)
            return
        }
        if (isSeeking) {
            pendingSeek = positionMs
            return
        }
        exoPlayer.seekTo(mediaItemIndex, positionMs)
    }

    override fun seekToDefaultPosition() {
        clearSeeking()
        exoPlayer.seekToDefaultPosition()
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        clearSeeking()
        exoPlayer.seekToDefaultPosition(mediaItemIndex)
    }

    override fun seekBack() {
        clearSeeking()
        exoPlayer.seekBack()
    }

    override fun seekForward() {
        clearSeeking()
        exoPlayer.seekForward()
    }

    override fun seekToNext() {
        clearSeeking()
        exoPlayer.seekToNext()
    }

    override fun seekToPrevious() {
        clearSeeking()
        exoPlayer.seekToPrevious()
    }

    override fun seekToNextMediaItem() {
        clearSeeking()
        exoPlayer.seekToNextMediaItem()
    }

    override fun seekToPreviousMediaItem() {
        clearSeeking()
        exoPlayer.seekToPreviousMediaItem()
    }

    /**
     * Releases the player.
     * This method must be called when the player is no longer required. The player must not be used after calling this method.
     *
     * Release call automatically [stop] if the player is not in [Player.STATE_IDLE].
     */
    override fun release() {
        clearSeeking()
        if (playbackState != Player.STATE_IDLE) {
            stop()
        }
        exoPlayer.release()
    }

    /**
     * Handle audio focus with currently set AudioAttributes
     * @param handleAudioFocus true if the player should handle audio focus, false otherwise.
     */
    fun setHandleAudioFocus(handleAudioFocus: Boolean) {
        setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        if (isPlaybackSpeedPossibleAtPosition(currentPosition, playbackParameters.speed, window)) {
            exoPlayer.playbackParameters = playbackParameters
        } else {
            exoPlayer.playbackParameters = playbackParameters.withSpeed(NormalSpeed)
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        playbackParameters = playbackParameters.withSpeed(speed)
    }

    private fun seekEnd() {
        isSeeking = false
        pendingSeek?.let { pendingPosition ->
            Log.d("PillarboxPlayer", "seek end in ${System.currentTimeMillis() - lastSeekTime} ms")
            seekTo(pendingPosition)
        }
        pendingSeek = null
    }

    private fun clearSeeking() {
        isSeeking = false
        pendingSeek = null
    }

    private inner class ComponentListener : Player.Listener {
        private val window = Window()

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            clearSeeking()
        }

        override fun onRenderedFirstFrame() {
            seekEnd()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (isSeeking) {
                        seekEnd()
                    }
                }

                Player.STATE_IDLE, Player.STATE_ENDED -> {
                    clearSeeking()
                }

                Player.STATE_BUFFERING -> {
                    // Do nothing
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            clearSeeking()
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                setPlaybackSpeed(NormalSpeed)
                seekToDefaultPosition()
                prepare()
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (!player.isCurrentMediaItemLive || player.getPlaybackSpeed() == NormalSpeed) return
            if (!player.isCurrentMediaItemSeekable) {
                setPlaybackSpeed(NormalSpeed)
                return
            }
            player.currentTimeline.getWindow(currentMediaItemIndex, window)
            if (window.isAtDefaultPosition(currentPosition) && getPlaybackSpeed() > NormalSpeed) {
                exoPlayer.setPlaybackSpeed(NormalSpeed)
            }
        }
    }
}

/**
 * Return if the playback [speed] is possible at [position].
 * Always return true for none live content or if [Player.getCurrentTimeline] is empty.
 *
 * @param position The position to test the playback speed.
 * @param speed The playback speed
 * @param window optional window for performance purpose
 * @return true if the playback [speed] can be set at [position]
 */
fun Player.isPlaybackSpeedPossibleAtPosition(position: Long, speed: Float, window: Window = Window()): Boolean {
    if (currentTimeline.isEmpty || speed == NormalSpeed || !isCurrentMediaItemLive) {
        return true
    }
    currentTimeline.getWindow(currentMediaItemIndex, window)
    return window.isPlaybackSpeedPossibleAtPosition(position, speed)
}

internal fun Window.isPlaybackSpeedPossibleAtPosition(positionMs: Long, playbackSpeed: Float): Boolean {
    return when {
        !isLive() || playbackSpeed == NormalSpeed -> true
        !isSeekable -> false
        isAtDefaultPosition(positionMs) && playbackSpeed > NormalSpeed -> false
        else -> true
    }
}

internal fun Window.isAtDefaultPosition(positionMs: Long): Boolean {
    return positionMs >= defaultPositionMs
}

private const val NormalSpeed = 1.0f
