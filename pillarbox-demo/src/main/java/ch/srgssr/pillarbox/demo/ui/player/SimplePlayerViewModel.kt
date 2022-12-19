/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Application
import android.util.Log
import android.util.Rational
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Dependencies
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple player view model than handle a PillarboxPlayer [player]
 */
class SimplePlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {
    /**
     * Player as PillarboxPlayer
     */
    val player = PillarboxPlayer(
        context = application,
        mediaItemSource = Dependencies.provideMixedItemSource(application),
        /**
         * If you plan to play some SRG Token protected content
         */
        dataSourceFactory = AkamaiTokenDataSource.Factory()
    )

    private val _pauseOnBackground = MutableStateFlow(true)
    private val _displayNotification = MutableStateFlow(false)

    /**
     * Pause on background state
     * True means playback is paused when Activity goes in background
     */
    val pauseOnBackground: StateFlow<Boolean> = _pauseOnBackground

    /**
     * Picture in picture enabled
     */
    val pictureInPictureEnabled = MutableStateFlow(false)

    /**
     * Picture in picture aspect ratio
     */
    var pictureInPictureRatio = MutableStateFlow(Rational(1, 1))

    init {
        player.addListener(this)
        /*
         * Seems to have no effect if not use with a foreground service to handle background playback.
         * Without service, playback may stop after ~ 1min with a socket time out.
         */
        player.setWakeMode(C.WAKE_MODE_NETWORK)

        /*
       * Will pause player when hp are disconnected
       */
        player.setHandleAudioBecomingNoisy(true)

        /*
         * When handleAudioFocus = true, will pause media when interrupted.
         * Playback will resume depending of the "importance" of the interruption (call, playback)
         */
        player.setHandleAudioFocus(true)
    }

    /**
     * Add to [player] all [items] to the MediaItem list.
     * Will prepare and play the content.
     *
     * @param items to play
     */
    fun playUri(items: List<DemoItem>) {
        player.setMediaItems(items.map { it.toMediaItem() })
        player.prepare()
        player.play()
    }

    /**
     * Toggle pause on background
     */
    fun togglePauseOnBackground() {
        _pauseOnBackground.value = !_pauseOnBackground.value
    }

    /**
     * Toggle display notification
     */
    fun toggleDisplayNotification() {
        _displayNotification.value = !_displayNotification.value
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared => releasing the player")
        player.release()
        player.removeListener(this)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        val rational = if (videoSize == VideoSize.UNKNOWN) {
            Rational(1, 1)
        } else {
            Rational(videoSize.width, videoSize.height)
        }
        pictureInPictureRatio.value = rational
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.d(TAG, "onMediaMetadataChanged title = ${mediaMetadata.title}")
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.d(TAG, "onPlaylistMetadataChanged title = ${mediaMetadata.title}")
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        val reasonString = when (reason) {
            Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED"
            Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "TIMELINE_CHANGE_REASON_SOURCE_UPDATE"
            else -> "?"
        }
        Log.d(
            TAG,
            "onTimelineChanged $reasonString ${player.currentMediaItem?.mediaId}" +
                " ${player.currentMediaItem?.mediaMetadata?.title}" +
                " uri = ${player.currentMediaItem?.localConfiguration?.uri}" +
                " tag=${player.currentMediaItem?.localConfiguration?.tag}"
        )
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        val reasonString = when (reason) {
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY"
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS"
            Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> "PLAY_WHEN_READY_CHANGE_REASON_REMOTE"
            Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> "PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST"
            Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> "PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM"
            else -> "?"
        }
        Log.d(TAG, "onPlayWhenReadyChanged $reasonString ${player.currentMediaItem?.mediaMetadata?.title}")
    }

    override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
        val stateString = when (playbackState) {
            Player.STATE_IDLE -> "STATE_IDLE"
            Player.STATE_READY -> "STATE_READY"
            Player.STATE_BUFFERING -> "STATE_BUFFERING"
            Player.STATE_ENDED -> "STATE_ENDED"
            else -> "?"
        }
        Log.d(TAG, "onPlaybackStateChanged $stateString ${player.currentMediaItem?.mediaMetadata?.title}")
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "onPlayerError", error)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        Log.d(TAG, "onPlayerErrorChanged $error")
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Log.d(TAG, "onPlaybackParametersChanged ${playbackParameters.speed}")
    }

    companion object {
        private const val TAG = "PillarboxDemo"
    }
}
