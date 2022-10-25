/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.data.MediaItemSource
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory

/**
 * Pillarbox player wrapping and configuring a Exoplayer instance.
 *
 * @property exoPlayer underlying Exoplayer instance used by the wrapper.
 * @constructor Create empty Pillarbox player
 */
class PillarboxPlayer private constructor(private val exoPlayer: ExoPlayer) :
    ExoPlayer by exoPlayer {

    init {
        addAnalyticsListener(EventLogger())
        addListener(ComponentListener())
    }

    constructor(
        context: Context,
        mediaItemSource: MediaItemSource,
        dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory(),
        loadControl : LoadControl = DefaultLoadControl()
    ) : this(
        ExoPlayer.Builder(context)
            .setUsePlatformDiagnostics(false)
            // .setSeekBackIncrementMs(10000)
            // .setSeekForwardIncrementMs(10000)
            .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                PillarboxMediaSourceFactory(
                    mediaItemSource = mediaItemSource,
                    defaultMediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
                )
            )
            .build()
    )

    private inner class ComponentListener : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                seekToDefaultPosition()
                prepare()
            }
        }
    }
}
