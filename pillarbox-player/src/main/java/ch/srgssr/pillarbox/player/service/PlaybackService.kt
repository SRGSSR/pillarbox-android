/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.media3.common.C
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter

/**
 * Playback service that handles background playback and media notification for a player.
 *
 * **Permissions**
 *
 * Add the following permissions to your `AndroidManifest.xml`:
 * ```xml
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
 * ```
 *
 * **Service Declaration**
 *
 * Declare your [PlaybackService] in your `AndroidManifest.xml` as follows:
 * ```xml
 * <service android:name=".YourService" android:foregroundServiceType="mediaPlayback" />
 * ```
 *
 * **Limitations**
 *
 * - **Service Termination:** the service is stopped when the last [ServiceConnection] is unbound. This can occur, for example, if the binding is done
 * within an [Activity] without handling orientation changes. Each rotation could potentially kill the service.
 * - **External Service Integration:** the player is not seamlessly integrated with external services like Android Auto. For Android Auto, you would
 * need to create a [MediaLibraryService].
 *
 * **Usage**
 *
 * Subclass this abstract class and implement the [pendingIntent] method to provide a [PendingIntent] for the [MediaSession]'s session activity. You
 * can customize the notification by overriding the [createNotificationBuilder] and [onMediaSessionCreated] methods.
 */
abstract class PlaybackService : Service() {
    private val binder = ServiceBinder()
    private var player: PillarboxExoPlayer? = null
    private var mediaSession: MediaSession? = null
    protected lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = createNotificationBuilder()
            .setNotificationListener(NotificationListener())
            .build()
        notificationManager.setUseChronometer(false)
    }

    /**
     * Creates a [PlayerNotificationManager.Builder] for building the notification.
     *
     * This method can be overridden to customize the notification's appearance and behavior.
     *
     * @return A [PlayerNotificationManager.Builder] instance.
     */
    open fun createNotificationBuilder(): PlayerNotificationManager.Builder {
        return PlayerNotificationManager.Builder(this, DEFAULT_NOTIFICATION_ID, DEFAULT_CHANNEL_ID)
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(androidx.media3.session.R.string.default_notification_channel_name)
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

    /**
     * Sets the player to be connected to MediaNotification and [MediaSession].
     *
     * @param player The [PillarboxExoPlayer] instance to be linked with this [PlaybackService].
     */
    fun setPlayer(player: PillarboxExoPlayer) {
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
            notificationManager.setMediaSessionToken(it.platformToken)
        }
    }

    /**
     * Called when the [MediaSession] is being created, allowing for customization of the [MediaSession.Builder].
     *
     * **Note:** customization of [setSessionActivity][MediaSession.Builder.setSessionActivity] is not allowed through this method. The session
     * activity is determined by the [pendingIntent] provided during initialization.
     *
     * @param mediaSessionBuilder The builder for the [MediaSession].
     * @return The modified [MediaSession.Builder].
     *
     * @see pendingIntent
     */
    open fun onMediaSessionCreated(mediaSessionBuilder: MediaSession.Builder): MediaSession.Builder {
        return mediaSessionBuilder
    }

    /**
     * Returns a [PendingIntent] that will be used to launch an [Activity] specified by [MediaSession.setSessionActivity] when the user interacts with
     * a media notification.
     *
     * @return A [PendingIntent] to launch the session [Activity].
     */
    abstract fun pendingIntent(): PendingIntent

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * A [Binder] class for interacting with the [PlaybackService].
     */
    inner class ServiceBinder : Binder() {
        /**
         * Sets the [player] to be used by this [MediaSession] for background playback.
         *
         * @param player The [PillarboxExoPlayer] instance to be linked to the [MediaSession].
         */
        fun setPlayer(player: PillarboxExoPlayer) {
            this@PlaybackService.setPlayer(player)
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            ServiceCompat.stopForeground(this@PlaybackService, ServiceCompat.STOP_FOREGROUND_REMOVE)
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
