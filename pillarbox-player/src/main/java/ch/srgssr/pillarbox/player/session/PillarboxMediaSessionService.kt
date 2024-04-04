/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * `PillarboxMediaSessionService` implementation of [MediaSessionService].
 * It is the recommended way to make background playback for Android.
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
 * <service
 *     android:name=".service.DemoMediaSessionService"
 *     android:exported="true"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaSessionService" />
 *     </intent-filter>
 * </service>
 * ```
 *
 * Use [PillarboxMediaController.Builder] to connect this Service to a `PillarboxMediaController`:
 * ```kotlin
 * coroutineScope.launch(){
 *     val mediaController: PillarboxPlayer = PillarboxMediaController.Builder(application,DemoMediaLibraryService::class.java)
 *     doSomethingWith(mediaController)
 * }
 * ...
 * mediaController.release() // when mediaController no more needed.
 * ```
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class PillarboxMediaSessionService : MediaSessionService() {
    private var player: Player? = null
    private var mediaSession: PillarboxMediaSession? = null

    /**
     * Release on task removed
     */
    var releaseOnTaskRemoved = true

    /**
     * Set player to use with this Service.
     * @param player PillarboxPlayer to link to this service.
     * @param mediaSessionCallback The MediaSession.Callback to use [MediaSession.Builder.setCallback].
     */
    fun setPlayer(
        player: PillarboxExoPlayer,
        mediaSessionCallback: PillarboxMediaSession.Callback = PillarboxMediaSession.Callback.Default
    ) {
        if (this.player == null) {
            this.player = player
            player.setWakeMode(C.WAKE_MODE_NETWORK)
            player.setHandleAudioFocus(true)
            mediaSession = PillarboxMediaSession.Builder(this, player).apply {
                sessionActivity()?.let {
                    setSessionActivity(it)
                }
                setId("MediaService/$packageName")
                setCallback(mediaSessionCallback)
            }.build()
        }
    }

    /**
     * Session activity use with [mediaSession] called when [setPlayer]
     */
    open fun sessionActivity(): PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(this)

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

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

    // Return a MediaSession to link with the MediaController that is making
    // this request.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession?.mediaSession
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
