/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SourceUriChangeException
import java.io.IOException

/**
 * Pillarbox player wrapping and configuring a Exoplayer instance.
 *
 * @property exoPlayer underlying Exoplayer instance used by the wrapper.
 * @constructor Create empty Pillarbox player
 */
class PillarboxPlayer private constructor(private val exoPlayer: ExoPlayer) :
    ExoPlayer by exoPlayer {
    private val componentListener = ComponentListener()

    init {
        addListener(componentListener)
        addAnalyticsListener(componentListener)
        addAnalyticsListener(EventLogger())
    }

    constructor(context: Context, mediaItemSource: MediaItemSource) : this(
        ExoPlayer.Builder(context)
            .setUsePlatformDiagnostics(false)
            // .setSeekBackIncrementMs(10000)
            // .setSeekForwardIncrementMs(10000)
            .setTrackSelector(DefaultTrackSelector(context))
            .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
            .setLoadControl(DefaultLoadControl())
            .setMediaSourceFactory(
                PillarboxMediaSourceFactory(
                    mediaItemSource = mediaItemSource,
                    defaultMediaSourceFactory = DefaultMediaSourceFactory(context)
                )
            )
            .build()
    )

    /**
     * Set play when ready
     *
     * Stop the player if the current item is a LiveStream without Dvr.
     *
     * @param playWhenReady
     */
    override fun setPlayWhenReady(playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
        Log.d(TAG, "setPlayWhenReadyy to $playWhenReady isLive=${isCurrentMediaItemLiveOnly()}")
        if (!playWhenReady && isCurrentMediaItemLiveOnly()) {
            stop()
        }
    }

    override fun pause() {
        playWhenReady = false
    }

    override fun play() {
        playWhenReady = true
    }

    private inner class ComponentListener : Player.Listener, AnalyticsListener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (!hasNextMediaItem() && playbackState == Player.STATE_ENDED) {
                // Stop the player when state is ended and no more item to play.
                // Guaranty to stop continuous update at the end of the media.
                stop()
            }
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            val window = Timeline.Window()
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            Log.d(TAG, "Analytics : onPlaybackStateChanged ${window.mediaItem.mediaId} ${window.mediaItem.mediaMetadata.title}")
        }

        override fun onLoadError(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) {
            Log.w(TAG, "Analytics : onLoadError", error)
            when (error) {
                // TODO handle "BlockReason" Exception or PlaybackInteruptException(code,message)
                is SourceUriChangeException -> {
                    Log.d(TAG, "Source uri changed !")
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

    /**
     * Is current media item live only
     *
     * @return true if the [getCurrentMediaItem] is live and not seekable
     */
    fun ExoPlayer.isCurrentMediaItemLiveOnly(): Boolean {
        return isCurrentMediaItemLive && !isCurrentMediaItemSeekable
    }

    companion object {
        private const val TAG = "PillarboxPlayer"
    }
}
