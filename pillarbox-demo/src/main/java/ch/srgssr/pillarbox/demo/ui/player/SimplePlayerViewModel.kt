/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Application
import android.util.Log
import android.util.Rational
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.asset.SkipableTimeInterval
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.extension.toRational
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.URL

/**
 * Simple player view model than handle a PillarboxPlayer [player]
 */
class SimplePlayerViewModel(
    application: Application,
    private val ilHost: URL
) : AndroidViewModel(application), PillarboxPlayer.Listener {
    /**
     * Player as PillarboxPlayer
     */
    val player = PlayerModule.provideDefaultPlayer(application)

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
        player.setMediaItems(items.map { it.toMediaItem(ilHost) })
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared => releasing the player")
        player.release()
        player.removeListener(this)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        pictureInPictureRatio.value = videoSize.toRational()
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

    override fun onCurrentChapterChanged(chapter: Chapter?) {
        Log.i(TAG, "onCurrentChapterChanged $chapter")
    }

    override fun onTimeIntervalChanged(timeInterval: SkipableTimeInterval?) {
        Log.i(TAG, "onTimeIntervalChanged $timeInterval")
    }

    companion object {
        private const val TAG = "PillarboxDemo"
    }

    /**
     * Factory to create [SimplePlayerViewModel].
     */
    class Factory(private val application: Application, private val ilHost: URL) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SimplePlayerViewModel(application, ilHost) as T
        }
    }
}
