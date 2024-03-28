/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.player.exoplayer.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaLibrarySession
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * `PillarboxMediaLibraryService` implementation of [MediaLibraryService].
 * It is the recommended way to make background playback for Android and sharing content with Android Auto.
 *
 * It handles only one [MediaSession] with one [PillarboxExoPlayer].
 *
 * Usage:
 * Add these permissions inside your manifest:
 *
 * ```xml
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
 * ```
 *
 * And add your `PlaybackService` to the application manifest as follow:
 *
 * ```xml
 * <meta-data android:name="com.google.android.gms.car.application" android:resource="@xml/automotive_app_desc" />
 *
 * <service
 *     android:name=".service.DemoMediaLibraryService"
 *     android:enabled="true"
 *     android:exported="true"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaLibraryService" />
 *         <action android:name="android.media.browse.MediaBrowserService" />
 *     </intent-filter>
 * </service>
 * ```
 *
 * Use [MediaBrowser.Builder][androidx.media3.session.MediaBrowser.Builder] to connect this Service to a `MediaBrowser`:
 * ```kotlin
 * val sessionToken = SessionToken(context, ComponentName(application, DemoMediaLibraryService::class.java))
 * val listenableFuture = MediaBrowser.Builder(context, sessionToken)
 *     .setListener(MediaBrowser.Listener()...) // Optional
 *     .buildAsync()
 * coroutineScope.launch(){
 *     val mediaBrowser = listenableFuture.await() // suspend method to retrieve MediaBrowser
 *     doSomethingWith(mediaBrowser)
 * }
 * ...
 * mediaBrowser.release() // when MediaBrowser no more needed.
 * ```
 */
abstract class PillarboxMediaLibraryService : MediaLibraryService() {
    private var player: Player? = null
    private var mediaSession: PillarboxMediaLibrarySession? = null

    /**
     * Release on task removed
     */
    var releaseOnTaskRemoved = true

    /**
     * Set player to use with this Service.
     */
    fun setPlayer(player: PillarboxExoPlayer, callback: PillarboxMediaLibrarySession.Callback) {
        if (this.player == null) {
            this.player = player
            player.setWakeMode(C.WAKE_MODE_NETWORK)
            player.setHandleAudioFocus(true)
            mediaSession = PillarboxMediaLibrarySession.Builder(this, player, callback).apply {
                setId(packageName)
                sessionActivity()?.let {
                    setSessionActivity(it)
                }
            }
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession?.mediaSession
    }

    /**
     * Session activity use with [mediaSession] called when [setPlayer]
     */
    open fun sessionActivity(): PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(this)

    /**
     * Release the player and the MediaSession.
     * The [mediaSession] is set to null after this call
     *
     * called automatically in [onDestroy] and [onTaskRemoved] is [releaseOnTaskRemoved] = true
     */
    open fun release() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    /**
     * We choose to stop playback when user remove application from the tasks
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (releaseOnTaskRemoved) {
            release()
            stopSelf()
        }
    }
}
