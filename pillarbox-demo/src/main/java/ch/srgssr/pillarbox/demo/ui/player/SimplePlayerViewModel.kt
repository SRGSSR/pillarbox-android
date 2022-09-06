/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.demo.data.DemoItemDataSource
import ch.srgssr.pillarbox.demo.data.DemoMediaItemSource
import ch.srgssr.pillarbox.demo.data.MixedMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Simple player view model than handle a PillarboxPlayer [player]
 */
class SimplePlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {
    /**
     * Player as PillarboxPlayer
     */
    val player = PillarboxPlayer(
        application,
        MixedMediaItemSource(
            DemoMediaItemSource(DemoItemDataSource(application)),
            MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(application, IlHost.PROD))
        )
    )

    init {
        player.addListener(this)
    }

    /**
     * Add to [player] all [ids] to the MediaItem list.
     * Will prepare and play the content.
     *
     * @param ids mediaIdentifier to play
     */
    fun playItemIds(ids: Array<String>) {
        player.addMediaItems(ids.map { fromMediaId(it) })
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        player.removeListener(this)
    }

    /**
     * Resume playback of [player]
     */
    fun resumePlayback() {
        player.play()
    }

    /**
     * Pause playback of [player]
     */
    fun pausePlayback() {
        player.pause()
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

        private fun fromMediaId(mediaIdentifier: String): MediaItem {
            return MediaItem.Builder().setMediaId(mediaIdentifier).build()
        }
    }
}
