/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Playback service to handle background playback
 *
 * You have to bind to service and call [PlaybackService.setPlayer] to link player to the service.
 *
 * Source : https://stackoverflow.com/questions/73052245/play-music-in-background-with-media3
 */
class PlaybackService : Service() {
    private val binder = ServiceBinder()
    private var player: Player? = null
    private var mediaSession: MediaSession? = null
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, "Pillarbox channel")
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.app_name)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setNotificationListener(NotificationListener())
            .build()
        notificationManager.setUseChronometer(true)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        notificationManager.setPlayer(null)
        player?.stop()
        mediaSession?.release()
        player?.release()
        mediaSession = null
        player = null
        stopSelf()
        ServiceCompat.stopForeground(this@PlaybackService, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    /**
     * Set player to be connected to MediaNotification and MediaSession.
     *
     * @param player
     */
    fun setPlayer(player: PillarboxPlayer) {
        player.setWakeMode(C.WAKE_MODE_NETWORK)
        player.setAudioAttributes(player.audioAttributes, true)
        notificationManager.setPlayer(player)
        if (mediaSession == null) {
            mediaSession = MediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent())
                .build()
        } else {
            mediaSession!!.player = player
        }
        mediaSession?.let {
            notificationManager.setMediaSessionToken(it.sessionCompatToken as MediaSessionCompat.Token)
        }
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, SimplePlayerActivity::class.java)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Service binder to retrieve [PlaybackService]
     */
    inner class ServiceBinder : Binder() {
        /**
         * @return [PlaybackService] to call [PlaybackService.setPlayer]
         */
        fun getPlaybackService(): PlaybackService = this@PlaybackService
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled by the user = $dismissedByUser")
            ServiceCompat.stopForeground(this@PlaybackService, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }

        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            Log.d(TAG, "onNotificationPosted ongoing = $ongoing")
            startForeground(notificationId, notification)
        }
    }

    companion object {
        private const val TAG = "PlaybackService"
        private const val NOTIFICATION_ID = 1002
    }
}
