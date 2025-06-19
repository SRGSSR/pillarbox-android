/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.media3.common.C
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.MediaSession
import androidx.media3.session.R
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * Demo playback service
 *
 * It doesn't stop playback after 1min while in the background and maintains a MediaNotification.
 */
class DemoPlaybackService : Service() {

    private val binder = ServiceBinder()
    private var player: PillarboxExoPlayer? = null
    private var mediaSession: PillarboxMediaSession? = null
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = createNotificationBuilder()
            .setNotificationListener(NotificationListener())
            .build()
        notificationManager.setUseChronometer(false)
    }

    private fun createNotificationBuilder(): PlayerNotificationManager.Builder {
        return PlayerNotificationManager.Builder(this, DEFAULT_NOTIFICATION_ID, DEFAULT_CHANNEL_ID)
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.default_notification_channel_name)
            .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(context = this, pendingIntent = pendingIntent()))
    }

    override fun onDestroy() {
        notificationManager.setPlayer(null)
        player?.stop()
        mediaSession?.release()
        mediaSession = null
        player = null
        stopSelf()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun setPlayer(player: PillarboxExoPlayer) {
        if (this.player != player) {
            this.player?.setWakeMode(C.WAKE_MODE_NONE)
            player.setWakeMode(C.WAKE_MODE_NETWORK)
            player.setHandleAudioFocus(true)
            this.player = player
        }
        notificationManager.setPlayer(player)
        if (mediaSession == null) {
            mediaSession = PillarboxMediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent())
                .build()
        }
        mediaSession?.let {
            it.player = player
            notificationManager.setMediaSessionToken(it.token)
        }
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, SimplePlayerActivity::class.java)
        val flags = PendingIntentUtils.appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            flags
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Binder class for [DemoPlaybackService].
     */
    inner class ServiceBinder : Binder() {
        /**
         * Sets the [player] to be used by this [MediaSession] for background playback.
         *
         * @param player The [PillarboxExoPlayer] instance to be linked to the [MediaSession].
         */
        fun setPlayer(player: PillarboxExoPlayer) {
            this@DemoPlaybackService.setPlayer(player)
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            ServiceCompat.stopForeground(this@DemoPlaybackService, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }

        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            startForeground(notificationId, notification)
        }
    }

    private companion object {
        private const val DEFAULT_NOTIFICATION_ID = 2023
        private const val DEFAULT_CHANNEL_ID = "Pillarbox now playing"
    }
}
