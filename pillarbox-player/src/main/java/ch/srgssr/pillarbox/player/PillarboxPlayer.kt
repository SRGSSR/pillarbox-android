/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import kotlin.time.Duration.Companion.milliseconds

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
    }

    constructor(
        context: Context,
        mediaItemSource: MediaItemSource,
        dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory(),
        loadControl: LoadControl = DefaultLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository()
    ) : this(
        ExoPlayer.Builder(context)
            .setUsePlatformDiagnostics(false)
            // .setSeekBackIncrementMs(10000)
            // .setSeekForwardIncrementMs(10000)
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
            .build(),
        mediaItemTrackerProvider = mediaItemTrackerProvider
    )

    /**
     * Handle audio focus with currently set AudioAttributes
     * @param handleAudioFocus true if the player should handle audio focus, false otherwise.
     */
    fun setHandleAudioFocus(handleAudioFocus: Boolean) {
        setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    private inner class ComponentListener : Player.Listener {
        private val window = Window()

        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                setPlaybackSpeed(1.0f)
                seekToDefaultPosition()
                prepare()
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            Log.i("Coucou", "playback speed changes ${playbackParameters.speed}")
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (!player.availableCommands.containsAny(
                    Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                    Player.COMMAND_GET_TIMELINE,
                    Player.COMMAND_SET_SPEED_AND_PITCH
                )
            ) return
            if (!player.isCurrentMediaItemLive) return
            if (player.getPlaybackSpeed() == 1.0f) return
            if (!player.isCurrentMediaItemSeekable) {
                setPlaybackSpeed(1.0f)
                return
            }
            player.currentTimeline.getWindow(currentMediaItemIndex, window)
            val liveEdgePosition = window.defaultPositionMs
            Log.d(
                "Coucou",
                "edge = ${liveEdgePosition.milliseconds} pos = ${player.currentPosition.milliseconds} timeshift = ${player.currentLiveOffset.milliseconds}"
            )
            if (currentPosition >= liveEdgePosition) {
                Log.w("Coucou", "Adjust speed!")
                setPlaybackSpeed(1.0f)
            }
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
