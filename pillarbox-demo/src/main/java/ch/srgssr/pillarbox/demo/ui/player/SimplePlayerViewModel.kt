/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.util.Rational
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.R
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.extension.toRational
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlinx.coroutines.flow.MutableStateFlow

private const val NotificationId = 2025

/**
 * Simple player view model that handles a PillarboxPlayer [player].
 * Playback notification is displayed when the player is in the foreground.
 * When the Activity goes to the background, the playback is stopped and the notification removed.
 */
class SimplePlayerViewModel(application: Application) : AndroidViewModel(application), PillarboxPlayer.Listener {
    /**
     * Player as PillarboxPlayer
     */
    val player = PlayerModule.provideDefaultPlayer(application)

    private val mediaSession = PillarboxMediaSession.Builder(application, player)
        .setSessionActivity(pendingIntent())
        .build()
    private val notificationManager: PlayerNotificationManager

    /**
     * Picture in picture enabled
     */
    val pictureInPictureEnabled = MutableStateFlow(false)

    /**
     * Picture in picture aspect ratio
     */
    var pictureInPictureRatio = MutableStateFlow(Rational(1, 1))

    init {
        notificationManager = PlayerNotificationManager.Builder(application, NotificationId, "Pillarbox now playing")
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.default_notification_channel_name)
            .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(context = application, pendingIntent = pendingIntent()))
            // .setNotificationListener(NotificationListener())
            .build()
        notificationManager.setUseChronometer(false)
        player.addListener(this)
        /*
         * Seems to have no effect if not use with a foreground service to handle background playback.
         * Without service, playback may stop after ~ 1min with a socket time out.
         */
        player.setWakeMode(C.WAKE_MODE_NETWORK)

        /*
         * Will pause player when headphones are disconnected
         */
        player.setHandleAudioBecomingNoisy(true)

        /*
         * When handleAudioFocus = true, will pause media when interrupted.
         * Playback will resume depending on the "importance" of the interruption (call, playback)
         */
        player.setHandleAudioFocus(true)
        notificationManager.setMediaSessionToken(mediaSession.token)
    }

    /**
     * Resume playback.
     */
    fun resumePlayback() {
        displayNotification()
        player.prepare()
        player.play()
    }

    /**
     * Stop playback.
     */
    fun stopPlayback() {
        player.stop()
        hideNotification()
    }

    private fun displayNotification() {
        notificationManager.setPlayer(player)
    }

    private fun hideNotification() {
        notificationManager.setPlayer(null)
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

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared => releasing the player")
        notificationManager.setPlayer(null)
        mediaSession.release()
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
        val reasonString = StringUtil.timelineChangeReasonString(reason)
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
        val stateString = StringUtil.playerStateString(playbackState)
        Log.d(TAG, "onPlaybackStateChanged $stateString ${player.currentMediaItem?.mediaMetadata?.title}")
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "onPlayerError", error)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        Log.d(TAG, "onPlayerErrorChanged", error)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Log.d(TAG, "onPlaybackParametersChanged ${playbackParameters.speed}")
    }

    override fun onChapterChanged(chapter: Chapter?) {
        Log.i(TAG, "onChapterChanged $chapter")
    }

    override fun onCreditChanged(credit: Credit?) {
        Log.i(TAG, "onCreditChanged $credit")
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(application, SimplePlayerActivity::class.java)
        val flags = PendingIntentUtils.appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT)
        return PendingIntent.getActivity(
            application,
            0,
            intent,
            flags
        )
    }

    private companion object {
        private const val TAG = "PillarboxDemo"
    }
}
