/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.extension.setPreferredAudioRoleFlagsToAccessibilityManagerSettings
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository

/**
 * Pillarbox player
 *
 * @property exoPlayer
 * @constructor
 *
 * @param mediaItemTrackerProvider
 */
class PillarboxPlayer internal constructor(
    private val exoPlayer: ExoPlayer,
    mediaItemTrackerProvider: MediaItemTrackerProvider? = null
) :
    ExoPlayer by exoPlayer {
    private val itemTracker: CurrentMediaItemTracker?
    private val window = Window()

    /**
     * Enable or disable MediaItem tracking
     */
    var trackingEnabled: Boolean
        set(value) = itemTracker?.let { it.enabled = value } ?: Unit
        get() = itemTracker?.enabled ?: false

    init {
        addListener(ComponentListener())
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
        loadControl: LoadControl = DefaultLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository(),
        seekIncrement: SeekIncrement = SeekIncrement()
    ) : this(
        ExoPlayer.Builder(context)
            .setUsePlatformDiagnostics(false)
            .setSeekBackIncrementMs(seekIncrement.backward.inWholeMilliseconds)
            .setSeekForwardIncrementMs(seekIncrement.forward.inWholeMilliseconds)
            .setRenderersFactory(
                DefaultRenderersFactory(context)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                    .setEnableDecoderFallback(true)
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
        mediaItemTrackerProvider = mediaItemTrackerProvider
    )

    /**
     * Releases the player.
     * This method must be called when the player is no longer required. The player must not be used after calling this method.
     *
     * Release call automatically [stop] if the player is not in [Player.STATE_IDLE].
     */
    override fun release() {
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

    private inner class ComponentListener : Player.Listener {
        private val window = Window()

        override fun onPlayerError(error: PlaybackException) {
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
 * Get a snapshot of the current media items
 */
fun Player.getCurrentMediaItems(): List<MediaItem> {
    if (mediaItemCount == 0) {
        return emptyList()
    }
    val count = mediaItemCount
    return ArrayList<MediaItem>(count).apply {
        for (i in 0 until count) {
            add(getMediaItemAt(i))
        }
    }
}

/**
 * Get playback speed
 *
 * @return [Player.getPlaybackParameters] speed
 */
fun Player.getPlaybackSpeed(): Float {
    return playbackParameters.speed
}

/**
 * Current position percent
 *
 * @return the current position in percent [0,1].
 */
fun Player.currentPositionPercent(): Float {
    return currentPosition / duration.coerceAtLeast(1).toFloat()
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

internal fun Window.isAtDefaultPosition(positionMs: Long): Boolean {
    return positionMs >= defaultPositionMs
}

internal fun Window.isPlaybackSpeedPossibleAtPosition(positionMs: Long, playbackSpeed: Float): Boolean {
    return when {
        !isLive() || playbackSpeed == NormalSpeed -> true
        !isSeekable -> false
        isAtDefaultPosition(positionMs) && playbackSpeed > NormalSpeed -> false
        else -> true
    }
}

private const val NormalSpeed = 1.0f
