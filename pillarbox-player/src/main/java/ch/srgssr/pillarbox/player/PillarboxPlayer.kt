/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SourceUriChangeException
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import ch.srgssr.pillarbox.player.utils.DebugLogger
import java.io.IOException

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
    private val componentListener = ComponentListener()

    /**
     * Enable or disable MediaItem tracking
     */
    var trackingEnabled: Boolean
        set(value) = itemTracker?.let { it.enabled = value } ?: Unit
        get() = itemTracker?.enabled ?: false

    init {
        addListener(componentListener)
        addAnalyticsListener(componentListener)
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

    private inner class ComponentListener : Player.Listener, AnalyticsListener {

        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                seekToDefaultPosition()
                prepare()
            }
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            DebugLogger.debug(TAG, "onPlaylistMetadataChanged ${mediaMetadata.title}")
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                val metadata = timeline.getWindow(currentMediaItemIndex, Timeline.Window()).mediaItem.mediaMetadata
                DebugLogger.debug(
                    TAG,
                    "onTimelineChanged ${metadata.title}"
                )
                playlistMetadata = metadata
            }
        }

        override fun onLoadError(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) {
            DebugLogger.warning(TAG, "onLoadError", error)
            when (error) {
                // TODO handle "BlockReason" Exception or PlaybackInteruptException(code,message)
                is SourceUriChangeException -> {
                    DebugLogger.debug(TAG, "Source uri changed !")
                    stop()
                }
                else -> {
                    createMessage { messageType, message ->
                        throw ExoPlaybackException.createForSource(error, ExoPlaybackException.ERROR_CODE_IO_UNSPECIFIED)
                    }.send()
                }
            }
        }
    }

    companion object {
        private const val TAG = "PillarboxPlayer"
    }
}
