/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

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
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter

/**
 * Playback service that handle background playback and Media notification for a *Player*.
 *
 * Add this permission inside your manifest :
 *
 * ```xml
 *      <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * ```
 * And add your PlaybackService to the application manifest as follow :
 *
 * ```xml
 *      <service android:name=".YourService" android:foregroundServiceType="mediaPlayback" />
 * ```
 *
 * Drawbacks :
 *  Then last ServiceConnection is unbind, it kills the service. Can happen if binding to service is done inside the Activity without
 *  orientationChanges. So each time user rotate, it's kills the service.
 *
 *  The player is not well integrated with external service like Android Auto. Has for AndroidAuto you have to create a MediaLibraryService.
 */
abstract class PlaybackService : Service() {
    private val binder = ServiceBinder()
    private var player: PillarboxPlayer? = null
    private var mediaSession: MediaSession? = null
    protected lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        notificationManager = createNotificationBuilder()
            .setNotificationListener(NotificationListener())
            .build()
        notificationManager.setUseChronometer(false)
    }

    /**
     * Create notification builder, can be override to customize it.
     *
     * @return
     */
    open fun createNotificationBuilder(): PlayerNotificationManager.Builder {
        return PlayerNotificationManager.Builder(this, DEFAULT_NOTIFICATION_ID, DEFAULT_CHANNEL_ID)
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(androidx.media3.session.R.string.default_notification_channel_name)
            .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(this, pendingIntent()))
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
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    /**
     * Set player to be connected to MediaNotification and MediaSession.
     *
     * @param player Player to be linked with this PlaybackService
     */
    fun setPlayer(player: PillarboxPlayer) {
        if (this.player != player) {
            this.player?.setWakeMode(C.WAKE_MODE_NONE)
            player.setWakeMode(C.WAKE_MODE_NETWORK)
            player.setHandleAudioFocus(true)
            this.player = player
        }
        notificationManager.setPlayer(player)
        if (mediaSession == null) {
            mediaSession = onMediaSessionCreated(
                MediaSession.Builder(this, player)
                    .setSessionActivity(pendingIntent())
            ).build()
        }
        mediaSession?.let {
            it.player = player
            notificationManager.setMediaSessionToken(it.sessionCompatToken as MediaSessionCompat.Token)
        }
    }

    /**
     * Allow [MediaSession.Builder] customization except [MediaSession.Builder.setSessionActivity]
     * @see pendingIntent
     */
    open fun onMediaSessionCreated(mediaSessionBuilder: MediaSession.Builder): MediaSession.Builder {
        return mediaSessionBuilder
    }

    /**
     * Pending intent for [MediaSession.getSessionActivity]
     */
    abstract fun pendingIntent(): PendingIntent

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Service binder to set Player
     */
    inner class ServiceBinder : Binder() {
        /**
         * Set [player] linked to this service [MediaSession] and to be handled for background playback.
         *
         * @param player
         */
        fun setPlayer(player: PillarboxPlayer) {
            this@PlaybackService.setPlayer(player)
        }
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
        private const val DEFAULT_NOTIFICATION_ID = 2023
        private const val DEFAULT_CHANNEL_ID = "Pillarbox now playing"
    }
}
