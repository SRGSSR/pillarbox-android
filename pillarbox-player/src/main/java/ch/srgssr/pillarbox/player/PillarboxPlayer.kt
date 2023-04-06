/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.MediaItem
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
     * We keep track on MediaMetadata change ourself.
     * Exoplayer doesn't change [Player.getMediaMetadata] when it is updated during playback.
     * The callback [Player.Listener.onMediaMetadataChanged] is called only once when the currentMediaItem has a localConfiguration.
     */
    private var _mediaMetadata: MediaMetadata = MediaMetadata.EMPTY

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
     * Get the current media metadata.
     * @ses [Player.getMediaMetadata]
     * @return the current media metata
     */
    override fun getMediaMetadata(): MediaMetadata {
        return if (_mediaMetadata != MediaMetadata.EMPTY) _mediaMetadata else exoPlayer.mediaMetadata
    }

    /**
     * Handle audio focus with currently set AudioAttributes
     * @param handleAudioFocus true if the player should handle audio focus, false otherwise.
     */
    fun setHandleAudioFocus(handleAudioFocus: Boolean) {
        setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    private fun clearMediaMetadata() {
        _mediaMetadata = MediaMetadata.EMPTY
    }

    private inner class ComponentListener : Player.Listener, AnalyticsListener {

        override fun onPlayerError(error: PlaybackException) {
            clearMediaMetadata()
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                seekToDefaultPosition()
                prepare()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (!hasNextMediaItem() && playbackState == Player.STATE_ENDED) {
                // Stop the player when state is ended and no more item to play.
                // Guaranty to stop continuous update at the end of the media.
                stop()
            }
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            DebugLogger.debug(TAG, "onPlaylistMetadataChanged ${mediaMetadata.title}")
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            DebugLogger.debug(TAG, "onMediaMetadataChanged ${mediaMetadata.title}")
            _mediaMetadata = mediaMetadata
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _mediaMetadata = mediaItem?.mediaMetadata ?: MediaMetadata.EMPTY
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE &&
                availableCommands.contains(Player.COMMAND_GET_TIMELINE) &&
                !timeline.isEmpty
            ) {
                val metadata = timeline.getWindow(currentMediaItemIndex, Timeline.Window()).mediaItem.mediaMetadata
                _mediaMetadata = metadata
                DebugLogger.debug(TAG, "onTimelineChanged ${metadata.title}")
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
