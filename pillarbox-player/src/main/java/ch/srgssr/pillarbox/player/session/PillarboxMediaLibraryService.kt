/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * `PillarboxMediaLibraryService` implementation of [MediaLibraryService].
 * It is the recommended way to make background playback for Android and sharing content with Android Auto.
 *
 * It handles only one [MediaSession] with one [PillarboxPlayer].
 *
 * Usage:
 * Add these permissions inside your manifest:
 *
 * ```xml
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
 * ```
 *
 * And add your `PillarboxMediaLibraryService` to the application manifest as follow:
 *
 * ```xml
 * <meta-data android:name="com.google.android.gms.car.application" android:resource="@xml/automotive_app_desc" />
 *
 * <service
 *     android:name=".service.DemoMediaLibraryService"
 *     android:enabled="true"
 *     android:exported="true"
 *     android:stopWithTask="true"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaLibraryService" />
 *         <action android:name="android.media.browse.MediaBrowserService" />
 *     </intent-filter>
 * </service>
 * ```
 *
 * Use [PillarboxMediaBrowser.Builder] to connect this Service to a [PillarboxMediaBrowser]:
 * ```kotlin
 * coroutineScope.launch() {
 *     val mediaBrowser = PillarboxMediaBrowser.Builder(application, DemoMediaLibraryService::class.java)
 *     doSomethingWith(mediaBrowser)
 * }
 * ...
 * mediaBrowser.release() // when the MediaBrowser is no longer needed.
 * ```
 */
abstract class PillarboxMediaLibraryService : MediaLibraryService() {
    private var mediaSession: PillarboxMediaLibrarySession? = null

    /**
     * Set player to use with this Service.
     * @param player [PillarboxPlayer] to link to this service.
     * @param callback The [PillarboxMediaLibrarySession.Callback]
     * @param sessionId The ID. Must be unique among all sessions per package.
     */
    fun setPlayer(
        player: PillarboxPlayer,
        callback: PillarboxMediaLibrarySession.Callback,
        sessionId: String? = null,
    ) {
        if (this.mediaSession == null) {
            player.setHandleAudioFocus(true)
            mediaSession = PillarboxMediaLibrarySession.Builder(this, player, callback).apply {
                sessionActivity()?.let {
                    setSessionActivity(it)
                }
                sessionId?.let {
                    setId(it)
                }
            }.build()
        } else {
            mediaSession?.player = player
        }
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaLibrarySession? {
        return mediaSession?.mediaSession
    }

    /**
     * Session activity use with [mediaSession] called when [setPlayer]
     */
    open fun sessionActivity(): PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(this)

    /**
     * Release the player and the MediaSession.
     * The [mediaSession] is set to null after this call.
     * Called automatically in [onDestroy]
     */
    open fun release() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }
}
