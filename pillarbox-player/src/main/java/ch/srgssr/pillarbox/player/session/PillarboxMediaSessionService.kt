/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * `PillarboxMediaSessionService` implementation of [MediaSessionService].
 * It is the recommended way to make background playback for Android.
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
 * And add your `PlaybackService` to the application manifest as follow:
 *
 * ```xml
 * <service
 *     android:name=".service.DemoMediaSessionService"
 *     android:exported="true"
 *     android:stopWithTask="true"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaSessionService" />
 *     </intent-filter>
 * </service>
 * ```
 *
 * Use [PillarboxMediaController.Builder] to connect this Service to a [PillarboxMediaController]:
 * ```kotlin
 * coroutineScope.launch() {
 *     val mediaController: PillarboxPlayer = PillarboxMediaController.Builder(application, DemoMediaLibraryService::class.java)
 *     doSomethingWith(mediaController)
 * }
 * ...
 * mediaController.release() // when the MediaController is no longer needed.
 * ```
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class PillarboxMediaSessionService : MediaSessionService() {
    private var mediaSession: PillarboxMediaSession? = null

    /**
     * Set player to use with this Service.
     * @param player [PillarboxPlayer] to link to this service.
     * @param mediaSessionCallback The [PillarboxMediaSession.Callback]
     * @param sessionId The ID. Must be unique among all sessions per package.
     */
    fun setPlayer(
        player: PillarboxPlayer,
        mediaSessionCallback: PillarboxMediaSession.Callback = PillarboxMediaSession.Callback.Default,
        sessionId: String? = null,
    ) {
        if (this.mediaSession == null) {
            player.setHandleAudioFocus(true)
            mediaSession = PillarboxMediaSession.Builder(this, player).apply {
                sessionActivity()?.let {
                    setSessionActivity(it)
                }
                setCallback(mediaSessionCallback)
                sessionId?.let {
                    setId(it)
                }
            }.build()
        } else {
            mediaSession?.player = player
        }
    }

    /**
     * Session activity use with [mediaSession] called when [setPlayer]
     */
    open fun sessionActivity(): PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(this)

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    /**
     * Release the player and the MediaSession.
     * The [mediaSession] is set to null after this call
     * Called automatically in [onDestroy]
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
}
