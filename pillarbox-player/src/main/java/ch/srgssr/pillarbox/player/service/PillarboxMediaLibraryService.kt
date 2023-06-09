/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import android.app.PendingIntent
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * PillarboxMediaLibraryService implementation of [MediaLibraryService].
 * It is the recommended way to make background playback for Android and sharing content with android Auto.
 *
 * It handles only one [MediaSession] with one [PillarboxPlayer].
 * Usage :
 * Add this permission inside your manifest :
 *
 * ```xml
 *      <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * ```
 * And add your PlaybackService to the application manifest as follow :
 *
 * ```xml
 *        <meta-data android:name="com.google.android.gms.car.application" android:resource="@xml/automotive_app_desc" />
 *
 *        <service
 *          android:name=".service.DemoMediaLibraryService"
 *          android:enabled = "true"
 *          android:exported="true"
 *          android:foregroundServiceType="mediaPlayback">
 *          <intent-filter>
 *              <action android:name="androidx.media3.session.MediaLibraryService" />
 *              <action android:name="android.media.browse.MediaBrowserService" />
 *          </intent-filter>
 *         </service>
 * ```
 *
 * Use [androidx.media3.session.MediaBrowser.Builder] to connect this Service to a *MediaBrowser*.
 * ```kotlin
 *      val sessionToken = SessionToken(context,ComponentName(application, DemoMediaLibraryService::class.java))
 *      val listenableFuture = MediaBrowser.Builder(context, sessionToken)
 *          .setListener(MediaBrowser.Listener()...) // Optional
 *          .buildAsync()
 *      coroutineScope.launch(){
 *          val mediaBrowser = listenableFuture.await() //suspend method to retrieve MediaBrowser
 *          doSomethingWith(mediaBrowser)
 *       }
 *      ...
 *      mediaBrowser.release() when MediaBrowser no more needed.
 * ```
 */
abstract class PillarboxMediaLibraryService : MediaLibraryService() {
    private var player: Player? = null
    private var mediaSession: MediaLibrarySession? = null

    /**
     * Release on task removed
     */
    var releaseOnTaskRemoved = true

    /**
     * Set player to use with this Service.
     */
    fun setPlayer(player: PillarboxPlayer, callback: MediaLibrarySession.Callback) {
        if (this.player == null) {
            this.player = null
            player.setWakeMode(C.WAKE_MODE_NETWORK)
            player.setHandleAudioFocus(true)
            val builder = MediaLibrarySession.Builder(this, player, callback)
                .setId(packageName)
            sessionActivity()?.let {
                builder.setSessionActivity(it)
            }
            mediaSession = builder.build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
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
            mediaSession = null
        }
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }
}
